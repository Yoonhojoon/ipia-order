package com.ipia.order.payment.intent.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.ipia.order.payment.intent.domain.PaymentIntent;
import com.ipia.order.payment.intent.repository.PaymentIntentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentIntentServiceImpl implements PaymentIntentService {

    private final PaymentIntentRepository paymentIntentRepository;

    @Override
    public void store(String intentId, Long orderId, BigDecimal amount, String successUrl, String failUrl, String idempotencyKey, long ttlSeconds) {
        PaymentIntent entity = PaymentIntent.create(intentId, orderId, amount, successUrl, failUrl, idempotencyKey, ttlSeconds);
        paymentIntentRepository.save(entity);
        log.info("PaymentIntent DB 저장: {}", intentId);
    }

    @Override
    public PaymentIntentData get(String intentId) {
        return paymentIntentRepository.findByIntentId(intentId)
                .map(entity -> {
                    if (entity.isExpired()) {
                        try { paymentIntentRepository.delete(entity); } catch (Exception ignored) {}
                        return null;
                    }
                    return new PaymentIntentData(
                            entity.getIntentId(),
                            entity.getOrderId(),
                            entity.getAmount(),
                            entity.getSuccessUrl(),
                            entity.getFailUrl(),
                            entity.getIdempotencyKey()
                    );
                })
                .orElse(null);
    }

    @Override
    public void delete(String intentId) {
        paymentIntentRepository.findByIntentId(intentId).ifPresent(paymentIntentRepository::delete);
    }
}


