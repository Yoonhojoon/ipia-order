package com.ipia.order.payment.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ipia.order.payment.service.external.TossCancelResponse;
import com.ipia.order.payment.service.external.TossConfirmResponse;
import com.ipia.order.payment.service.external.TossPaymentClient;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private TossPaymentClient tossPaymentClient;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Nested
    @DisplayName("approve")
    class approveTests {
        @Test
        @DisplayName("승인: 금액 불일치 시 예외 (미구현 스텁)")
        void approve_amountMismatch_shouldThrow() {
            // then (현재 미구현 스텁이므로 UnsupportedOperationException 기대)
            assertThatThrownBy(() -> paymentService.approve(
                    "intent-1",
                    "paymentKey-xyz",
                    1L,
                    new BigDecimal("9999")
            ))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("승인: 성공 플로우 (현재 미구현 스텁으로 예외 기대)")
        void approve_success_placeholder() {
            when(tossPaymentClient.confirm("paymentKey-ok", "1", new BigDecimal("10000")))
                    .thenReturn(new TossConfirmResponse("paymentKey-ok", "1", new BigDecimal("10000")));

            assertThatCode(() -> paymentService.approve(
                    "intent-ok", "paymentKey-ok", 1L, new BigDecimal("10000")
            )).isInstanceOf(UnsupportedOperationException.class);
        }

    }


    @Nested
    @DisplayName("verify")
    class VerifyTests {
        @Test
        @DisplayName("의도와 order/amount 불일치 시 예외 (미구현 스텁)")
        void mismatch_shouldThrow() {
            assertThatThrownBy(() -> paymentService.verify(
                    "intent-1",
                    "paymentKey-xyz",
                    2L,
                    new BigDecimal("10000")
            ))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("성공: 일치 시 통과 (현재 미구현 스텁으로 예외 기대)")
        void success_placeholder() {
            assertThatThrownBy(() -> paymentService.verify(
                    "intent-ok",
                    "paymentKey-ok",
                    1L,
                    new BigDecimal("10000")
            ))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("cancel")
    class CancelTests {
        @Test
        @DisplayName("승인 전/불가 상태에서 취소 요청 시 예외 (미구현 스텁)")
        void invalidState_shouldThrow() {
            assertThatThrownBy(() -> paymentService.cancel(
                    "paymentKey-xyz",
                    new BigDecimal("1000"),
                    "고객요청"
            ))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("취소 금액 초과: 사전 검증 실패 가정 (미구현 스텁)")
        void amountExceeded_shouldThrow() {
            assertThatThrownBy(() -> paymentService.cancel(
                    "paymentKey-xyz",
                    new BigDecimal("999999"),
                    "사유"
            ))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("성공: 정상 취소 (현재 미구현 스텁으로 예외 기대)")
        void success_placeholder() {
            when(tossPaymentClient.cancel("paymentKey-ok", new BigDecimal("1000"), "고객요청"))
                    .thenReturn(new TossCancelResponse("paymentKey-ok", new BigDecimal("1000"), "CANCELED"));

            assertThatThrownBy(() -> paymentService.cancel(
                    "paymentKey-ok", new BigDecimal("1000"), "고객요청"
            )).isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("createIntent")
    class CreateIntentTests {
        @Test
        @DisplayName("음수/0 금액으로 의도 생성 시 예외 (미구현 스텁)")
        void invalidAmount_shouldThrow() {
            assertThatThrownBy(() -> paymentService.createIntent(
                    1L, new BigDecimal("0"), "s", "f", "idem"
            ))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("성공: 정상 의도 생성 (현재 미구현 스텁으로 예외 기대)")
        void success_placeholder() {
            assertThatThrownBy(() -> paymentService.createIntent(
                    1L, new BigDecimal("10000"), "s", "f", "idem"
            ))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("approve - 추가 케이스")
    class ApproveMoreTests {
        @Test
        @DisplayName("이미 승인된 결제 재승인 시도 예외 (미구현 스텁)")
        void reapprove_shouldThrow() {
            assertThatThrownBy(() -> paymentService.approve(
                    "intent-approved", "paymentKey-abc", 1L, new BigDecimal("10000")
            )).isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("승인 불가능한 상태에서 승인 시도 예외 (미구현 스텁)")
        void invalidState_shouldThrow() {
            assertThatThrownBy(() -> paymentService.approve(
                    "intent-canceled", "paymentKey-abc", 1L, new BigDecimal("10000")
            )).isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("Toss 4xx 매핑 테스트 (미구현 스텁)")
        void toss4xx_shouldThrowMapped() {
            assertThatThrownBy(() -> paymentService.approve(
                    "intent-4xx", "paymentKey-4xx", 1L, new BigDecimal("10000")
            )).isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("Toss 5xx/타임아웃 재시도 및 초과 실패 (미구현 스텁)")
        void toss5xx_retry_shouldThrow() {
            assertThatThrownBy(() -> paymentService.approve(
                    "intent-5xx", "paymentKey-5xx", 1L, new BigDecimal("10000")
            )).isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("취소 - 추가 케이스")
    class CancelMoreTests {
        @Test
        @DisplayName("취소 불가능한 상태에서 취소 시도 예외 (미구현 스텁)")
        void invalidState_cancel_shouldThrow() {
            assertThatThrownBy(() -> paymentService.cancel(
                    "paymentKey-not-approved", new BigDecimal("1000"), "사유"
            )).isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("Toss 4xx 매핑 테스트 (미구현 스텁)")
        void toss4xx_cancel_shouldThrowMapped() {
            assertThatThrownBy(() -> paymentService.cancel(
                    "paymentKey-4xx", new BigDecimal("1000"), "사유"
            )).isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("Toss 5xx/타임아웃 재시도 및 초과 실패 (미구현 스텁)")
        void toss5xx_cancel_retry_shouldThrow() {
            assertThatThrownBy(() -> paymentService.cancel(
                    "paymentKey-5xx", new BigDecimal("1000"), "사유"
            )).isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("데이터 검증")
    class ValidationTests {
        @Test
        @DisplayName("paymentKey null/blank 예외 (미구현 스텁)")
        void invalidPaymentKey_shouldThrow() {
            assertThatThrownBy(() -> paymentService.approve(
                    "intent", "", 1L, new BigDecimal("10000")
            )).isInstanceOf(UnsupportedOperationException.class);

            assertThatThrownBy(() -> paymentService.cancel(
                    null, new BigDecimal("1000"), "사유"
            )).isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("orderId 0/음수 예외 (미구현 스텁)")
        void invalidOrderId_shouldThrow() {
            assertThatThrownBy(() -> paymentService.approve(
                    "intent", "paymentKey", 0L, new BigDecimal("10000")
            )).isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("amount 음수/0 예외 (미구현 스텁)")
        void invalidAmount_shouldThrow() {
            assertThatThrownBy(() -> paymentService.approve(
                    "intent", "paymentKey", 1L, new BigDecimal("0")
            )).isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("외부 의존성/재시도 정책")
    class ExternalDependencyTests {
        @Test
        @DisplayName("네트워크 오류/타임아웃 재시도 정책 (미구현 스텁)")
        void networkTimeout_retry_shouldThrow() {
            assertThatThrownBy(() -> paymentService.approve(
                    "intent-timeout", "paymentKey-timeout", 1L, new BigDecimal("10000")
            )).isInstanceOf(UnsupportedOperationException.class);
        }
    }
}


