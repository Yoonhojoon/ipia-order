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
}


