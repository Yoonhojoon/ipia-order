package com.ipia.order.payment.domain;

import com.ipia.order.common.exception.payment.PaymentHandler;
import com.ipia.order.common.exception.payment.status.PaymentErrorStatus;
import com.ipia.order.payment.enums.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Payment 엔티티의 비즈니스 로직을 테스트하는 클래스
 */
class PaymentTest {

    private static final Long ORDER_ID = 1L;
    private static final BigDecimal ORDER_TOTAL_AMOUNT = new BigDecimal("10000");
    private static final BigDecimal PAYMENT_AMOUNT = new BigDecimal("10000");
    private static final String PROVIDER_TXN_ID = "toss_payment_key_12345";

    @Nested
    @DisplayName("Payment 생성 테스트")
    class CreatePaymentTest {

        @Test
        @DisplayName("정상적인 결제 생성")
        void createPayment_Success() {
            // when
            Payment payment = PaymentTestBuilder.builder()
                    .orderId(ORDER_ID)
                    .paidAmount(PAYMENT_AMOUNT)
                    .providerTxnId(PROVIDER_TXN_ID)
                    .build();

            // then
            assertThat(payment.getOrderId()).isEqualTo(ORDER_ID);
            assertThat(payment.getPaidAmount()).isEqualTo(PAYMENT_AMOUNT);
            assertThat(payment.getProviderTxnId()).isEqualTo(PROVIDER_TXN_ID);
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(payment.getCanceledAmount()).isEqualTo(BigDecimal.ZERO);
            assertThat(payment.getRefundedAmount()).isEqualTo(BigDecimal.ZERO);
            assertThat(payment.getApprovedAt()).isNull();
            assertThat(payment.getCanceledAt()).isNull();
            assertThat(payment.getRefundedAt()).isNull();
        }

        @Test
        @DisplayName("결제 생성 시 상태 확인 메서드들이 올바르게 동작")
        void createPayment_StatusCheckMethods() {
            // when
            Payment payment = PaymentTestBuilder.builder()
                    .orderId(ORDER_ID)
                    .paidAmount(PAYMENT_AMOUNT)
                    .providerTxnId(PROVIDER_TXN_ID)
                    .build();

            // then
            assertThat(payment.isPending()).isTrue();
            assertThat(payment.isApproved()).isFalse();
            assertThat(payment.isCanceled()).isFalse();
            assertThat(payment.isRefunded()).isFalse();
        }
    }

    @Nested
    @DisplayName("결제 승인 테스트")
    class ApprovePaymentTest {

        @Test
        @DisplayName("정상적인 결제 승인")
        void approvePayment_Success() {
            // given
            Payment payment = Payment.create(ORDER_ID, PAYMENT_AMOUNT, PROVIDER_TXN_ID);

            // when
            payment.approve(ORDER_TOTAL_AMOUNT);

            // then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
            assertThat(payment.getApprovedAt()).isNotNull();
            assertThat(payment.isApproved()).isTrue();
            assertThat(payment.isPending()).isFalse();
        }

        @Test
        @DisplayName("결제 금액이 주문 총액과 일치하지 않으면 예외 발생")
        void approvePayment_AmountMismatch_ThrowsException() {
            // given
            Payment payment = Payment.create(ORDER_ID, PAYMENT_AMOUNT, PROVIDER_TXN_ID);
            BigDecimal differentAmount = new BigDecimal("5000");

            // when & then
            assertThatThrownBy(() -> payment.approve(differentAmount))
                .isInstanceOf(PaymentHandler.class)
                .hasFieldOrPropertyWithValue("status", PaymentErrorStatus.PAYMENT_AMOUNT_MISMATCH);
        }

        @Test
        @DisplayName("이미 승인된 결제를 다시 승인하려 하면 예외 발생")
        void approvePayment_AlreadyApproved_ThrowsException() {
            // given
            Payment payment = PaymentTestBuilder.builder()
                    .orderId(ORDER_ID)
                    .paidAmount(PAYMENT_AMOUNT)
                    .providerTxnId(PROVIDER_TXN_ID)
                    .status(PaymentStatus.APPROVED)
                    .approvedAt(LocalDateTime.now())
                    .build();

            // when & then
            assertThatThrownBy(() -> payment.approve(ORDER_TOTAL_AMOUNT))
                .isInstanceOf(PaymentHandler.class)
                .hasFieldOrPropertyWithValue("status", PaymentErrorStatus.PAYMENT_CANNOT_APPROVE);
        }

        @Test
        @DisplayName("취소된 결제를 승인하려 하면 예외 발생")
        void approvePayment_CanceledPayment_ThrowsException() {
            // given
            Payment payment = PaymentTestBuilder.builder()
                    .orderId(ORDER_ID)
                    .paidAmount(PAYMENT_AMOUNT)
                    .providerTxnId(PROVIDER_TXN_ID)
                    .status(PaymentStatus.CANCELED)
                    .canceledAmount(PAYMENT_AMOUNT)
                    .canceledAt(LocalDateTime.now())
                    .build();

            // when & then
            assertThatThrownBy(() -> payment.approve(ORDER_TOTAL_AMOUNT))
                .isInstanceOf(PaymentHandler.class)
                .hasFieldOrPropertyWithValue("status", PaymentErrorStatus.PAYMENT_CANNOT_APPROVE);
        }

        @Test
        @DisplayName("환불된 결제를 승인하려 하면 예외 발생")
        void approvePayment_RefundedPayment_ThrowsException() {
            // given
            Payment payment = PaymentTestBuilder.builder()
                    .orderId(ORDER_ID)
                    .paidAmount(PAYMENT_AMOUNT)
                    .providerTxnId(PROVIDER_TXN_ID)
                    .status(PaymentStatus.REFUNDED)
                    .canceledAmount(PAYMENT_AMOUNT)
                    .refundedAmount(PAYMENT_AMOUNT)
                    .canceledAt(LocalDateTime.now())
                    .refundedAt(LocalDateTime.now())
                    .build();

            // when & then
            assertThatThrownBy(() -> payment.approve(ORDER_TOTAL_AMOUNT))
                .isInstanceOf(PaymentHandler.class)
                .hasFieldOrPropertyWithValue("status", PaymentErrorStatus.PAYMENT_CANNOT_APPROVE);
        }
    }

    @Nested
    @DisplayName("결제 취소 테스트")
    class CancelPaymentTest {

        @Test
        @DisplayName("정상적인 결제 취소")
        void cancelPayment_Success() {
            // given
            Payment payment = Payment.create(ORDER_ID, PAYMENT_AMOUNT, PROVIDER_TXN_ID);
            payment.approve(ORDER_TOTAL_AMOUNT);
            String cancelReason = "고객 요청";

            // when
            payment.cancel(PAYMENT_AMOUNT, cancelReason);

            // then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELED);
            assertThat(payment.getCanceledAmount()).isEqualTo(PAYMENT_AMOUNT);
            assertThat(payment.getCanceledAt()).isNotNull();
            assertThat(payment.isCanceled()).isTrue();
            assertThat(payment.isApproved()).isFalse();
        }

        @Test
        @DisplayName("취소 금액이 0 이하면 예외 발생")
        void cancelPayment_InvalidAmount_ThrowsException() {
            // given
            Payment payment = Payment.create(ORDER_ID, PAYMENT_AMOUNT, PROVIDER_TXN_ID);
            payment.approve(ORDER_TOTAL_AMOUNT);

            // when & then
            assertThatThrownBy(() -> payment.cancel(BigDecimal.ZERO, "고객 요청"))
                .isInstanceOf(PaymentHandler.class)
                .hasFieldOrPropertyWithValue("status", PaymentErrorStatus.INVALID_CANCEL_AMOUNT);
        }

        @Test
        @DisplayName("취소 금액이 결제 금액을 초과하면 예외 발생")
        void cancelPayment_ExceedAmount_ThrowsException() {
            // given
            Payment payment = Payment.create(ORDER_ID, PAYMENT_AMOUNT, PROVIDER_TXN_ID);
            payment.approve(ORDER_TOTAL_AMOUNT);
            BigDecimal exceedAmount = new BigDecimal("15000");

            // when & then
            assertThatThrownBy(() -> payment.cancel(exceedAmount, "고객 요청"))
                .isInstanceOf(PaymentHandler.class)
                .hasFieldOrPropertyWithValue("status", PaymentErrorStatus.CANCEL_AMOUNT_EXCEEDED);
        }

        @Test
        @DisplayName("대기 상태의 결제를 취소하려 하면 예외 발생")
        void cancelPayment_PendingPayment_ThrowsException() {
            // given
            Payment payment = Payment.create(ORDER_ID, PAYMENT_AMOUNT, PROVIDER_TXN_ID);

            // when & then
            assertThatThrownBy(() -> payment.cancel(PAYMENT_AMOUNT, "고객 요청"))
                .isInstanceOf(PaymentHandler.class)
                .hasFieldOrPropertyWithValue("status", PaymentErrorStatus.PAYMENT_CANNOT_CANCEL);
        }

        @Test
        @DisplayName("이미 취소된 결제를 다시 취소하려 하면 예외 발생")
        void cancelPayment_AlreadyCanceled_ThrowsException() {
            // given
            Payment payment = Payment.create(ORDER_ID, PAYMENT_AMOUNT, PROVIDER_TXN_ID);
            payment.approve(ORDER_TOTAL_AMOUNT);
            payment.cancel(PAYMENT_AMOUNT, "고객 요청");

            // when & then
            assertThatThrownBy(() -> payment.cancel(PAYMENT_AMOUNT, "고객 요청"))
                .isInstanceOf(PaymentHandler.class)
                .hasFieldOrPropertyWithValue("status", PaymentErrorStatus.PAYMENT_CANNOT_CANCEL);
        }

        @Test
        @DisplayName("환불된 결제를 취소하려 하면 예외 발생")
        void cancelPayment_RefundedPayment_ThrowsException() {
            // given
            Payment payment = Payment.create(ORDER_ID, PAYMENT_AMOUNT, PROVIDER_TXN_ID);
            payment.approve(ORDER_TOTAL_AMOUNT);
            payment.cancel(PAYMENT_AMOUNT, "고객 요청");
            payment.refund(PAYMENT_AMOUNT, "환불 요청");

            // when & then
            assertThatThrownBy(() -> payment.cancel(PAYMENT_AMOUNT, "고객 요청"))
                .isInstanceOf(PaymentHandler.class)
                .hasFieldOrPropertyWithValue("status", PaymentErrorStatus.PAYMENT_CANNOT_CANCEL);
        }
    }

    @Nested
    @DisplayName("결제 환불 테스트")
    class RefundPaymentTest {

        @Test
        @DisplayName("정상적인 결제 환불")
        void refundPayment_Success() {
            // given
            Payment payment = Payment.create(ORDER_ID, PAYMENT_AMOUNT, PROVIDER_TXN_ID);
            payment.approve(ORDER_TOTAL_AMOUNT);
            payment.cancel(PAYMENT_AMOUNT, "고객 요청");
            String refundReason = "환불 요청";

            // when
            payment.refund(PAYMENT_AMOUNT, refundReason);

            // then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
            assertThat(payment.getRefundedAmount()).isEqualTo(PAYMENT_AMOUNT);
            assertThat(payment.getRefundedAt()).isNotNull();
            assertThat(payment.isRefunded()).isTrue();
            assertThat(payment.isCanceled()).isFalse();
        }

        @Test
        @DisplayName("환불 금액이 0 이하면 예외 발생")
        void refundPayment_InvalidAmount_ThrowsException() {
            // given
            Payment payment = Payment.create(ORDER_ID, PAYMENT_AMOUNT, PROVIDER_TXN_ID);
            payment.approve(ORDER_TOTAL_AMOUNT);
            payment.cancel(PAYMENT_AMOUNT, "고객 요청");

            // when & then
            assertThatThrownBy(() -> payment.refund(BigDecimal.ZERO, "환불 요청"))
                .isInstanceOf(PaymentHandler.class)
                .hasFieldOrPropertyWithValue("status", PaymentErrorStatus.INVALID_REFUND_AMOUNT);
        }

        @Test
        @DisplayName("환불 금액이 취소 금액을 초과하면 예외 발생")
        void refundPayment_ExceedAmount_ThrowsException() {
            // given
            Payment payment = Payment.create(ORDER_ID, PAYMENT_AMOUNT, PROVIDER_TXN_ID);
            payment.approve(ORDER_TOTAL_AMOUNT);
            payment.cancel(PAYMENT_AMOUNT, "고객 요청");
            BigDecimal exceedAmount = new BigDecimal("15000");

            // when & then
            assertThatThrownBy(() -> payment.refund(exceedAmount, "환불 요청"))
                .isInstanceOf(PaymentHandler.class)
                .hasFieldOrPropertyWithValue("status", PaymentErrorStatus.REFUND_AMOUNT_EXCEEDED);
        }

        @Test
        @DisplayName("대기 상태의 결제를 환불하려 하면 예외 발생")
        void refundPayment_PendingPayment_ThrowsException() {
            // given
            Payment payment = Payment.create(ORDER_ID, PAYMENT_AMOUNT, PROVIDER_TXN_ID);

            // when & then
            assertThatThrownBy(() -> payment.refund(PAYMENT_AMOUNT, "환불 요청"))
                .isInstanceOf(PaymentHandler.class)
                .hasFieldOrPropertyWithValue("status", PaymentErrorStatus.PAYMENT_CANNOT_REFUND);
        }

        @Test
        @DisplayName("승인된 결제를 환불하려 하면 예외 발생")
        void refundPayment_ApprovedPayment_ThrowsException() {
            // given
            Payment payment = Payment.create(ORDER_ID, PAYMENT_AMOUNT, PROVIDER_TXN_ID);
            payment.approve(ORDER_TOTAL_AMOUNT);

            // when & then
            assertThatThrownBy(() -> payment.refund(PAYMENT_AMOUNT, "환불 요청"))
                .isInstanceOf(PaymentHandler.class)
                .hasFieldOrPropertyWithValue("status", PaymentErrorStatus.PAYMENT_CANNOT_REFUND);
        }

        @Test
        @DisplayName("이미 환불된 결제를 다시 환불하려 하면 예외 발생")
        void refundPayment_AlreadyRefunded_ThrowsException() {
            // given
            Payment payment = Payment.create(ORDER_ID, PAYMENT_AMOUNT, PROVIDER_TXN_ID);
            payment.approve(ORDER_TOTAL_AMOUNT);
            payment.cancel(PAYMENT_AMOUNT, "고객 요청");
            payment.refund(PAYMENT_AMOUNT, "환불 요청");

            // when & then
            assertThatThrownBy(() -> payment.refund(PAYMENT_AMOUNT, "환불 요청"))
                .isInstanceOf(PaymentHandler.class)
                .hasFieldOrPropertyWithValue("status", PaymentErrorStatus.PAYMENT_CANNOT_REFUND);
        }
    }

    @Nested
    @DisplayName("결제 상태 전이 전체 플로우 테스트")
    class PaymentFlowTest {

        @Test
        @DisplayName("전체 결제 플로우: 생성 → 승인 → 취소 → 환불")
        void completePaymentFlow_Success() {
            // given
            String cancelReason = "고객 요청";
            String refundReason = "환불 요청";

            // when - 1. 결제 생성
            Payment payment = Payment.create(ORDER_ID, PAYMENT_AMOUNT, PROVIDER_TXN_ID);
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);

            // when - 2. 결제 승인
            payment.approve(ORDER_TOTAL_AMOUNT);
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);

            // when - 3. 결제 취소
            payment.cancel(PAYMENT_AMOUNT, cancelReason);
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELED);

            // when - 4. 결제 환불
            payment.refund(PAYMENT_AMOUNT, refundReason);
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);

            // then - 최종 상태 검증
            assertThat(payment.getPaidAmount()).isEqualTo(PAYMENT_AMOUNT);
            assertThat(payment.getCanceledAmount()).isEqualTo(PAYMENT_AMOUNT);
            assertThat(payment.getRefundedAmount()).isEqualTo(PAYMENT_AMOUNT);
            assertThat(payment.getApprovedAt()).isNotNull();
            assertThat(payment.getCanceledAt()).isNotNull();
            assertThat(payment.getRefundedAt()).isNotNull();
            assertThat(payment.isRefunded()).isTrue();
        }
    }
}
