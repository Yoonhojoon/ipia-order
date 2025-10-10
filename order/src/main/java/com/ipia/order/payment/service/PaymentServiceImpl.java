package com.ipia.order.payment.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.ipia.order.common.exception.payment.PaymentHandler;
import com.ipia.order.common.exception.payment.status.PaymentErrorStatus;
import com.ipia.order.order.service.OrderService;
import com.ipia.order.payment.domain.Payment;
import com.ipia.order.payment.service.external.TossCancelResponse;
import com.ipia.order.payment.service.external.TossConfirmResponse;
import com.ipia.order.payment.service.external.TossPaymentClient;
import com.ipia.order.payment.service.port.PaymentIntentStore;
import com.ipia.order.payment.service.port.PaymentIntentStore.PaymentIntentData;
import com.ipia.order.payment.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final TossPaymentClient tossPaymentClient;
    private final PaymentIntentStore paymentIntentStore;
    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final ApplicationEventPublisher eventPublisher;

    private static final long INTENT_TTL_SECONDS = 1800; // 30분

    @Override
    @Transactional
    public String createIntent(long orderId, BigDecimal amount, String successUrl, String failUrl, String idempotencyKey) {
        log.info("결제 의도 생성 요청: orderId={}, amount={}, idempotencyKey={}", orderId, amount, idempotencyKey);
        
        // 입력값 검증
        validateCreateIntentParams(orderId, amount, successUrl, failUrl, idempotencyKey);
        
        // 의도 ID 생성
        String intentId = generateIntentId();
        
        // Redis에 의도 저장
        paymentIntentStore.store(intentId, orderId, amount, successUrl, failUrl, idempotencyKey, INTENT_TTL_SECONDS);
        
        log.info("결제 의도 생성 완료: intentId={}, orderId={}, amount={}", intentId, orderId, amount);
        return intentId;
    }

    @Override
    @Transactional(readOnly = true)
    public void verify(String intentId, String paymentKey, long orderId, BigDecimal amount) {
        log.info("결제 의도 검증 요청: intentId={}, paymentKey={}, orderId={}, amount={}", intentId, paymentKey, orderId, amount);
        
        // 입력값 검증
        validateVerifyParams(intentId, paymentKey, orderId, amount);
        
        // 의도 조회
        PaymentIntentData intent = paymentIntentStore.get(intentId);
        if (intent == null) {
            log.warn("결제 의도를 찾을 수 없음: intentId={}", intentId);
            throw new PaymentHandler(PaymentErrorStatus.PAYMENT_NOT_FOUND);
        }
        
        // 의도 데이터 검증
        if (!intent.orderId().equals(orderId)) {
            log.warn("주문 ID 불일치: intent.orderId={}, request.orderId={}", intent.orderId(), orderId);
            throw new PaymentHandler(PaymentErrorStatus.PAYMENT_AMOUNT_MISMATCH);
        }
        
        if (intent.amount().compareTo(amount) != 0) {
            log.warn("결제 금액 불일치: intent.amount={}, request.amount={}", intent.amount(), amount);
            throw new PaymentHandler(PaymentErrorStatus.PAYMENT_AMOUNT_MISMATCH);
        }
        
        log.info("결제 의도 검증 성공: intentId={}, orderId={}, amount={}", intentId, orderId, amount);
    }

    @Override
    @Transactional
    public Long approve(String intentId, String paymentKey, long orderId, BigDecimal amount) {
        log.info("결제 승인 요청: intentId={}, paymentKey={}, orderId={}, amount={}", intentId, paymentKey, orderId, amount);
        
        // 입력값 검증
        validateApproveParams(intentId, paymentKey, orderId, amount);
        
        // 의도 검증
        verify(intentId, paymentKey, orderId, amount);
        
        // 기존 결제 확인 (중복 승인 방지)
        paymentRepository.findByOrderId(orderId).ifPresent(payment -> {
            if (payment.isApproved()) {
                log.warn("이미 승인된 결제: orderId={}, paymentId={}", orderId, payment.getId());
                throw new PaymentHandler(PaymentErrorStatus.PAYMENT_ALREADY_APPROVED);
            }
        });
        
        // Toss API 호출
        TossConfirmResponse tossResponse = tossPaymentClient.confirm(paymentKey, String.valueOf(orderId), amount);
        
        // Payment 엔티티 생성 및 승인
        Payment payment = Payment.create(orderId, amount, paymentKey);
        payment.approve(amount);
        
        // 저장
        Payment savedPayment = paymentRepository.save(payment);
        
        // 의도 삭제
        paymentIntentStore.delete(intentId);
        
        // 주문 상태 업데이트 (결제 완료)
        orderService.handlePaymentApproved(orderId);
        
        log.info("결제 승인 완료: paymentId={}, orderId={}, amount={}", savedPayment.getId(), orderId, amount);
        return savedPayment.getId();
    }

    @Override
    @Transactional
    public void cancel(String paymentKey, BigDecimal cancelAmount, String reason) {
        log.info("결제 취소 요청: paymentKey={}, cancelAmount={}, reason={}", paymentKey, cancelAmount, reason);
        
        // 입력값 검증
        validateCancelParams(paymentKey, cancelAmount, reason);
        
        // Payment 조회
        Payment payment = paymentRepository.findByProviderTxnId(paymentKey)
                .orElseThrow(() -> new PaymentHandler(PaymentErrorStatus.PAYMENT_NOT_FOUND));
        
        // Toss API 호출
        TossCancelResponse tossResponse = tossPaymentClient.cancel(paymentKey, cancelAmount, reason);
        
        // Payment 취소 처리
        payment.cancel(cancelAmount, reason);
        paymentRepository.save(payment);
        
        // 주문 상태 업데이트 (결제 취소)
        orderService.handlePaymentCanceled(payment.getOrderId());
        
        log.info("결제 취소 완료: paymentId={}, orderId={}, cancelAmount={}", payment.getId(), payment.getOrderId(), cancelAmount);
    }

    // ==================== 검증 메서드 ====================

    private void validateCreateIntentParams(long orderId, BigDecimal amount, String successUrl, String failUrl, String idempotencyKey) {
        if (orderId <= 0) {
            throw new PaymentHandler(PaymentErrorStatus.ORDER_ID_REQUIRED);
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentHandler(PaymentErrorStatus.PAYMENT_AMOUNT_REQUIRED);
        }
        if (!StringUtils.hasText(successUrl)) {
            throw new PaymentHandler(PaymentErrorStatus.INVALID_IDEMPOTENCY_KEY);
        }
        if (!StringUtils.hasText(failUrl)) {
            throw new PaymentHandler(PaymentErrorStatus.INVALID_IDEMPOTENCY_KEY);
        }
        if (!StringUtils.hasText(idempotencyKey)) {
            throw new PaymentHandler(PaymentErrorStatus.INVALID_IDEMPOTENCY_KEY);
        }
    }

    private void validateVerifyParams(String intentId, String paymentKey, long orderId, BigDecimal amount) {
        if (!StringUtils.hasText(intentId)) {
            throw new PaymentHandler(PaymentErrorStatus.INVALID_IDEMPOTENCY_KEY);
        }
        if (!StringUtils.hasText(paymentKey)) {
            throw new PaymentHandler(PaymentErrorStatus.PROVIDER_TXN_ID_REQUIRED);
        }
        if (orderId <= 0) {
            throw new PaymentHandler(PaymentErrorStatus.ORDER_ID_REQUIRED);
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentHandler(PaymentErrorStatus.PAYMENT_AMOUNT_REQUIRED);
        }
    }

    private void validateApproveParams(String intentId, String paymentKey, long orderId, BigDecimal amount) {
        validateVerifyParams(intentId, paymentKey, orderId, amount);
    }

    private void validateCancelParams(String paymentKey, BigDecimal cancelAmount, String reason) {
        if (!StringUtils.hasText(paymentKey)) {
            throw new PaymentHandler(PaymentErrorStatus.PROVIDER_TXN_ID_REQUIRED);
        }
        if (cancelAmount == null || cancelAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentHandler(PaymentErrorStatus.INVALID_CANCEL_AMOUNT);
        }
        if (!StringUtils.hasText(reason)) {
            throw new PaymentHandler(PaymentErrorStatus.INVALID_IDEMPOTENCY_KEY);
        }
    }

    private String generateIntentId() {
        return "intent_" + UUID.randomUUID().toString().replace("-", "");
    }
}


