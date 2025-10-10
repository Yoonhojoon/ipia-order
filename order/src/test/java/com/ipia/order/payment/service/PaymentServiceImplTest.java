package com.ipia.order.payment.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import com.ipia.order.common.exception.payment.status.PaymentErrorStatus;
import com.ipia.order.payment.domain.Payment;
import com.ipia.order.payment.enums.PaymentStatus;
import com.ipia.order.payment.intent.service.PaymentIntentService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ipia.order.common.exception.payment.PaymentHandler;
import com.ipia.order.order.service.OrderService;
import com.ipia.order.payment.service.external.TossCancelResponse;
import com.ipia.order.payment.service.external.TossConfirmResponse;
import com.ipia.order.payment.service.external.TossPaymentClient;
import com.ipia.order.payment.domain.PaymentTestBuilder;
import com.ipia.order.payment.repository.PaymentRepository;
import com.ipia.order.idempotency.service.IdempotencyKeyService;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private TossPaymentClient tossPaymentClient;
    
    @Mock
    private PaymentIntentService paymentIntentService;
    
    @Mock
    private PaymentRepository paymentRepository;
    
    @Mock
    private OrderService orderService;

    @Mock
    private IdempotencyKeyService idempotencyKeyService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    

    @Nested
    @DisplayName("approve")
    class approveTests {
        @Test
        @DisplayName("승인: 금액 불일치 시 예외")
        void approve_amountMismatch_shouldThrow() {
            // given
            PaymentIntentService.PaymentIntentData intentData = new PaymentIntentService.PaymentIntentData(
                    "intent-1", 1L, new BigDecimal("10000"), "http://success", "http/fail", "idem"
            );
            when(paymentIntentService.get("intent-1")).thenReturn(intentData);
            
            // idempotencyKeyService가 실제 로직을 실행하도록 설정
            when(idempotencyKeyService.executeWithIdempotency(any(), any(), any(), any()))
                    .thenAnswer(inv -> {
                        java.util.function.Supplier<?> op = inv.getArgument(3);
                        return op.get(); // 실제 로직 실행
                    });
            
            // when & then
            assertThatThrownBy(() -> paymentService.approve(
                    "intent-1",
                    "paymentKey-xyz",
                    1L,
                    new BigDecimal("9999"),
                    "idem"
            ))
                    .isInstanceOf(PaymentHandler.class);
        }

        @Test
        @DisplayName("승인: 성공 플로우")
        void approve_success_shouldReturnPaymentId() {
            // given
            PaymentIntentService.PaymentIntentData intentData = new PaymentIntentService.PaymentIntentData(
                    "intent-ok", 1L, new BigDecimal("10000"), "http://success", "http://fail", "idem"
            );
            when(paymentIntentService.get("intent-ok")).thenReturn(intentData);
            when(tossPaymentClient.confirm("paymentKey-ok", "1", new BigDecimal("10000")))
                    .thenReturn(new TossConfirmResponse("paymentKey-ok", "1", new BigDecimal("10000")));
            when(paymentRepository.findByOrderId(1L)).thenReturn(java.util.Optional.empty());
            // PaymentTestBuilder를 사용하여 ID가 설정된 Payment 생성
            Payment savedPayment = PaymentTestBuilder.builder()
                    .id(123L)
                    .orderId(1L)
                    .paidAmount(new BigDecimal("10000"))
                    .providerTxnId("paymentKey-ok")
                    .build();
            when(paymentRepository.save(any())).thenReturn(savedPayment);

            // idempotencyKeyService가 실제 로직을 실행하도록 설정
            when(idempotencyKeyService.executeWithIdempotency(any(), any(), any(), any()))
                    .thenAnswer(inv -> {
                        java.util.function.Supplier<?> op = inv.getArgument(3);
                        return op.get(); // 실제 로직 실행
                    });

            // when
            Long paymentId = paymentService.approve(
                    "intent-ok", "paymentKey-ok", 1L, new BigDecimal("10000"), "idem"
            );

            // then
            assertThatCode(() -> {
                if (paymentId == null) {
                    throw new PaymentHandler(PaymentErrorStatus.PAYMENT_CANNOT_APPROVE);
                }
            }).doesNotThrowAnyException();
            verify(paymentIntentService).delete("intent-ok");
            verify(orderService).handlePaymentApproved(1L);
        }

    }


    @Nested
    @DisplayName("verify")
    class VerifyTests {
        @Test
        @DisplayName("의도와 order/amount 불일치 시 예외")
        void mismatch_shouldThrow() {
            // given
            PaymentIntentService.PaymentIntentData intentData = new PaymentIntentService.PaymentIntentData(
                    "intent-1", 1L, new BigDecimal("10000"), "http://success", "http://fail", "idem"
            );
            when(paymentIntentService.get("intent-1")).thenReturn(intentData);
            
            // when & then
            assertThatThrownBy(() -> paymentService.verify(
                    "intent-1",
                    "paymentKey-xyz",
                    2L,
                    new BigDecimal("10000"),
                    "idem"
            ))
                    .isInstanceOf(PaymentHandler.class);
        }

        @Test
        @DisplayName("성공: 일치 시 통과")
        void success_shouldPass() {
            // given
            PaymentIntentService.PaymentIntentData intentData = new PaymentIntentService.PaymentIntentData(
                    "intent-ok", 1L, new BigDecimal("10000"), "http://success", "http://fail", "idem"
            );
            when(paymentIntentService.get("intent-ok")).thenReturn(intentData);
            
            // when & then
            assertThatCode(() -> paymentService.verify(
                    "intent-ok",
                    "paymentKey-ok",
                    1L,
                    new BigDecimal("10000"),
                    "idem"
            )).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("cancel")
    class CancelTests {
        @Test
        @DisplayName("승인 전/불가 상태에서 취소 요청 시 예외")
        void invalidState_shouldThrow() {
            // given
            when(paymentRepository.findByProviderTxnId("paymentKey-xyz")).thenReturn(Optional.empty());
            
            // when & then
            assertThatThrownBy(() -> paymentService.cancel(
                    "paymentKey-xyz",
                    new BigDecimal("1000"),
                    "고객요청"
            ))
                    .isInstanceOf(PaymentHandler.class);
        }

        @Test
        @DisplayName("취소 금액 초과: 사전 검증 실패 가정")
        void amountExceeded_shouldThrow() {
            // given
            com.ipia.order.payment.domain.Payment payment = PaymentTestBuilder.builder()
                    .id(1L)
                    .orderId(1L)
                    .paidAmount(new BigDecimal("10000"))
                    .providerTxnId("paymentKey-xyz")
                    .status(PaymentStatus.APPROVED)
                    .build();
            when(paymentRepository.findByProviderTxnId("paymentKey-xyz")).thenReturn(Optional.of(payment));
            
            // when & then
            assertThatThrownBy(() -> paymentService.cancel(
                    "paymentKey-xyz",
                    new BigDecimal("999999"),
                    "사유"
            ))
                    .isInstanceOf(PaymentHandler.class);
        }

        @Test
        @DisplayName("성공: 정상 취소")
        void success_shouldComplete() {
            // given
            Payment payment = PaymentTestBuilder.builder()
                    .id(1L)
                    .orderId(1L)
                    .paidAmount(new BigDecimal("10000"))
                    .providerTxnId("paymentKey-ok")
                    .status(PaymentStatus.APPROVED)
                    .build();
            when(paymentRepository.findByProviderTxnId("paymentKey-ok")).thenReturn(Optional.of(payment));
            when(tossPaymentClient.cancel("paymentKey-ok", new BigDecimal("1000"), "고객요청"))
                    .thenReturn(new TossCancelResponse("paymentKey-ok", new BigDecimal("1000"), "CANCELED"));
            when(paymentRepository.save(any())).thenReturn(payment);

            // when & then
            assertThatCode(() -> {
                paymentService.cancel("paymentKey-ok", new BigDecimal("1000"), "고객요청");
            }).doesNotThrowAnyException();
            
            verify(orderService).handlePaymentCanceled(1L);
        }
    }

    @Nested
    @DisplayName("createIntent")
    class CreateIntentTests {
        @Test
        @DisplayName("음수/0 금액으로 의도 생성 시 예외")
        void invalidAmount_shouldThrow() {
            assertThatThrownBy(() -> paymentService.createIntent(
                    1L, new BigDecimal("0"), "s", "f", "test-idem-key"
            ))
                    .isInstanceOf(PaymentHandler.class);
        }

        @Test
        @DisplayName("성공: 정상 의도 생성")
        void success_shouldReturnIntentId() {
            // when
                when(idempotencyKeyService.executeWithIdempotency(any(), any(), any(), any()))
                        .thenAnswer(inv -> {
                            java.util.function.Supplier<?> op = inv.getArgument(3);
                            return op.get();
                        });
            String result = paymentService.createIntent(
                    1L, new BigDecimal("10000"), "http://success", "http://fail", "test-idem-key"
            );
            
            // then
            assertThatCode(() -> {
                // 이미 실행된 결과를 확인
                if (result == null || result.isEmpty()) {
                    throw new RuntimeException("Intent ID should not be null or empty");
                }
            }).doesNotThrowAnyException();
            verify(paymentIntentService).store(anyString(), eq(1L), eq(new BigDecimal("10000")), 
                    eq("http://success"), eq("http://fail"), anyString(), eq(1800L));
        }
    }

    @Nested
    @DisplayName("approve - 추가 케이스")
    class ApproveMoreTests {
        @Test
        @DisplayName("이미 승인된 결제 재승인 시도 예외")
        void reapprove_shouldThrow() {
            // given
            PaymentIntentService.PaymentIntentData intentData = new PaymentIntentService.PaymentIntentData(
                    "intent-approved", 1L, new BigDecimal("10000"), "http://success", "http://fail", "idem"
            );
            when(paymentIntentService.get("intent-approved")).thenReturn(intentData);
            
            Payment existingPayment = Payment.create(1L, new BigDecimal("10000"), "paymentKey-abc");
            existingPayment.approve(new BigDecimal("10000"));
            when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.of(existingPayment));
            
            // idempotencyKeyService가 실제 로직을 실행하도록 설정
            when(idempotencyKeyService.executeWithIdempotency(any(), any(), any(), any()))
                    .thenAnswer(inv -> {
                        java.util.function.Supplier<?> op = inv.getArgument(3);
                        return op.get(); // 실제 로직 실행
                    });
            
            // when & then
            assertThatThrownBy(() -> paymentService.approve(
                    "intent-approved", "paymentKey-abc", 1L, new BigDecimal("10000"), "idem"
            )).isInstanceOf(PaymentHandler.class);
        }

        @Test
        @DisplayName("승인 불가능한 상태에서 승인 시도 예외")
        void invalidState_shouldThrow() {
            // given
            when(paymentIntentService.get("intent-canceled")).thenReturn(null);
            
            // idempotencyKeyService가 실제 로직을 실행하도록 설정
            when(idempotencyKeyService.executeWithIdempotency(any(), any(), any(), any()))
                    .thenAnswer(inv -> {
                        java.util.function.Supplier<?> op = inv.getArgument(3);
                        return op.get(); // 실제 로직 실행
                    });
            
            // when & then
            assertThatThrownBy(() -> paymentService.approve(
                    "intent-canceled", "paymentKey-abc", 1L, new BigDecimal("10000"), "idem"
            )).isInstanceOf(PaymentHandler.class);
        }

        @Test
        @DisplayName("Toss 4xx 매핑 테스트")
        void toss4xx_shouldThrowMapped() {
            // given
            PaymentIntentService.PaymentIntentData intentData = new PaymentIntentService.PaymentIntentData(
                    "intent-4xx", 1L, new BigDecimal("10000"), "http://success", "http://fail", "idem"
            );
            when(paymentIntentService.get("intent-4xx")).thenReturn(intentData);
            when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());
            
            // TossPaymentClient에서 PaymentHandler 예외 발생하도록 설정
            when(tossPaymentClient.confirm("paymentKey-4xx", "1", new BigDecimal("10000")))
                    .thenThrow(new PaymentHandler(PaymentErrorStatus.TOSS_API_ERROR));
            
            // idempotencyKeyService가 실제 로직을 실행하도록 설정
            when(idempotencyKeyService.executeWithIdempotency(any(), any(), any(), any()))
                    .thenAnswer(inv -> {
                        java.util.function.Supplier<?> op = inv.getArgument(3);
                        return op.get(); // 실제 로직 실행
                    });
            
            // when & then
            assertThatThrownBy(() -> paymentService.approve(
                    "intent-4xx", "paymentKey-4xx", 1L, new BigDecimal("10000"), "idem"
            )).isInstanceOf(PaymentHandler.class);
        }

        @Test
        @DisplayName("Toss 5xx/타임아웃 재시도 및 초과 실패")
        void toss5xx_retry_shouldThrow() {
            // given
            PaymentIntentService.PaymentIntentData intentData = new PaymentIntentService.PaymentIntentData(
                    "intent-5xx", 1L, new BigDecimal("10000"), "http://success", "http://fail", "idem"
            );
            when(paymentIntentService.get("intent-5xx")).thenReturn(intentData);
            when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());
            
            // TossPaymentClient에서 PaymentHandler 예외 발생하도록 설정
            when(tossPaymentClient.confirm("paymentKey-5xx", "1", new BigDecimal("10000")))
                    .thenThrow(new PaymentHandler(PaymentErrorStatus.TOSS_NETWORK_ERROR));
            
            // idempotencyKeyService가 실제 로직을 실행하도록 설정
            when(idempotencyKeyService.executeWithIdempotency(any(), any(), any(), any()))
                    .thenAnswer(inv -> {
                        java.util.function.Supplier<?> op = inv.getArgument(3);
                        return op.get(); // 실제 로직 실행
                    });
            
            // when & then
            assertThatThrownBy(() -> paymentService.approve(
                    "intent-5xx", "paymentKey-5xx", 1L, new BigDecimal("10000"), "idem"
            )).isInstanceOf(PaymentHandler.class);
        }
    }

    @Nested
    @DisplayName("idempotency")
    class IdempotencyTests {
        @Test
        @DisplayName("같은 멱등키로 두 번 승인 요청하면 같은 결과 반환하고 외부호출은 한 번만")
        void approve_sameIdempotencyKey_returnsSameResult_and_callsOnce() {
            // given
            PaymentIntentService.PaymentIntentData intentData = new PaymentIntentService.PaymentIntentData(
                    "intent-idem", 1L, new BigDecimal("10000"), "http://success", "http://fail", "idem"
            );
            when(paymentIntentService.get("intent-idem")).thenReturn(intentData);
            when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());
            when(tossPaymentClient.confirm("paymentKey-idem", "1", new BigDecimal("10000")))
                    .thenReturn(new TossConfirmResponse("paymentKey-idem", "1", new BigDecimal("10000")));
            Payment saved = PaymentTestBuilder.builder()
                    .id(999L).orderId(1L).paidAmount(new BigDecimal("10000")).providerTxnId("paymentKey-idem").build();
            when(paymentRepository.save(any())).thenReturn(saved);

            final Long[] cached = new Long[1];
            when(idempotencyKeyService.executeWithIdempotency(eq("POST /payments/approve"), eq("idem"), eq(Long.class), any()))
                    .thenAnswer(inv -> {
                        java.util.function.Supplier<?> op = inv.getArgument(3);
                        if (cached[0] == null) {
                            cached[0] = (Long) op.get();
                        }
                        return cached[0];
                    });

            // when
            Long first = paymentService.approve("intent-idem", "paymentKey-idem", 1L, new BigDecimal("10000"), "idem");
            Long second = paymentService.approve("intent-idem", "paymentKey-idem", 1L, new BigDecimal("10000"), "idem");

            // then
            Assertions.assertThat(second).isEqualTo(first);
            // 멱등성 처리로 인해 실제 로직은 한 번만 실행됨
            verify(tossPaymentClient, times(1)).confirm("paymentKey-idem", "1", new BigDecimal("10000"));
            verify(paymentRepository, times(1)).save(any());
            verify(paymentIntentService, times(1)).delete("intent-idem");
            verify(orderService, times(1)).handlePaymentApproved(1L);
        }
    }

    @Nested
    @DisplayName("취소 - 추가 케이스")
    class CancelMoreTests {
        @Test
        @DisplayName("취소 불가능한 상태에서 취소 시도 예외")
        void invalidState_cancel_shouldThrow() {
            // given
            when(paymentRepository.findByProviderTxnId("paymentKey-not-approved")).thenReturn(Optional.empty());
            
            // when & then
            assertThatThrownBy(() -> paymentService.cancel(
                    "paymentKey-not-approved", new BigDecimal("1000"), "사유"
            )).isInstanceOf(PaymentHandler.class);
        }

        @Test
        @DisplayName("Toss 4xx 매핑 테스트")
        void toss4xx_cancel_shouldThrowMapped() {
            // given
            Payment payment = PaymentTestBuilder.builder()
                    .id(1L)
                    .orderId(1L)
                    .paidAmount(new BigDecimal("10000"))
                    .providerTxnId("paymentKey-4xx")
                    .status(com.ipia.order.payment.enums.PaymentStatus.APPROVED)
                    .build();
            when(paymentRepository.findByProviderTxnId("paymentKey-4xx")).thenReturn(Optional.of(payment));
            
            // TossPaymentClient에서 PaymentHandler 예외 발생하도록 설정
            when(tossPaymentClient.cancel("paymentKey-4xx", new BigDecimal("1000"), "사유"))
                    .thenThrow(new PaymentHandler(PaymentErrorStatus.TOSS_API_ERROR));
            
            // when & then
            assertThatThrownBy(() -> paymentService.cancel(
                    "paymentKey-4xx", new BigDecimal("1000"), "사유"
            )).isInstanceOf(PaymentHandler.class);
        }

        @Test
        @DisplayName("Toss 5xx/타임아웃 재시도 및 초과 실패")
        void toss5xx_cancel_retry_shouldThrow() {
            // given
            Payment payment = PaymentTestBuilder.builder()
                    .id(1L)
                    .orderId(1L)
                    .paidAmount(new BigDecimal("10000"))
                    .providerTxnId("paymentKey-5xx")
                    .status(com.ipia.order.payment.enums.PaymentStatus.APPROVED)
                    .build();
            when(paymentRepository.findByProviderTxnId("paymentKey-5xx")).thenReturn(Optional.of(payment));
            
            // TossPaymentClient에서 PaymentHandler 예외 발생하도록 설정
            when(tossPaymentClient.cancel("paymentKey-5xx", new BigDecimal("1000"), "사유"))
                    .thenThrow(new PaymentHandler(PaymentErrorStatus.TOSS_NETWORK_ERROR));
            
            // when & then
            assertThatThrownBy(() -> paymentService.cancel(
                    "paymentKey-5xx", new BigDecimal("1000"), "사유"
            )).isInstanceOf(PaymentHandler.class);
        }
    }

    @Nested
    @DisplayName("데이터 검증")
    class ValidationTests {
        @Test
        @DisplayName("paymentKey null/blank 예외")
        void invalidPaymentKey_shouldThrow() {
            assertThatThrownBy(() -> paymentService.approve(
                    "intent", "", 1L, new BigDecimal("10000"), "idem"
            )).isInstanceOf(PaymentHandler.class);

            assertThatThrownBy(() -> paymentService.cancel(
                    null, new BigDecimal("1000"), "사유"
            )).isInstanceOf(PaymentHandler.class);
        }

        @Test
        @DisplayName("orderId 0/음수 예외")
        void invalidOrderId_shouldThrow() {
            assertThatThrownBy(() -> paymentService.approve(
                    "intent", "paymentKey", 0L, new BigDecimal("10000"), "idem"
            )).isInstanceOf(PaymentHandler.class);
        }

        @Test
        @DisplayName("amount 음수/0 예외")
        void invalidAmount_shouldThrow() {
            assertThatThrownBy(() -> paymentService.approve(
                    "intent", "paymentKey", 1L, new BigDecimal("0"), "idem"
            )).isInstanceOf(PaymentHandler.class);
        }
    }

    @Nested
    @DisplayName("외부 의존성/재시도 정책")
    class ExternalDependencyTests {
        @Test
        @DisplayName("네트워크 오류/타임아웃 재시도 정책")
        void networkTimeout_retry_shouldThrow() {
            // given
            PaymentIntentService.PaymentIntentData intentData = new PaymentIntentService.PaymentIntentData(
                    "intent-timeout", 1L, new BigDecimal("10000"), "http://success", "http://fail", "idem"
            );
            when(paymentIntentService.get("intent-timeout")).thenReturn(intentData);
            when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());
            
            // TossPaymentClient에서 예외 발생하도록 설정 (네트워크 오류 시뮬레이션)
            when(tossPaymentClient.confirm("paymentKey-timeout", "1", new BigDecimal("10000")))
                    .thenThrow(new RuntimeException("Network timeout"));
            
            // idempotencyKeyService가 실제 로직을 실행하도록 설정
            when(idempotencyKeyService.executeWithIdempotency(any(), any(), any(), any()))
                    .thenAnswer(inv -> {
                        java.util.function.Supplier<?> op = inv.getArgument(3);
                        return op.get(); // 실제 로직 실행
                    });
            
            // when & then
            assertThatThrownBy(() -> paymentService.approve(
                    "intent-timeout", "paymentKey-timeout", 1L, new BigDecimal("10000"), "idem"
            )).isInstanceOf(RuntimeException.class);
        }
    }
}


