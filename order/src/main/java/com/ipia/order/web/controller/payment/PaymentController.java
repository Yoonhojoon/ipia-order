package com.ipia.order.web.controller.payment;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ipia.order.common.exception.ApiErrorCodeExample;
import com.ipia.order.common.exception.ApiErrorCodeExamples;
import com.ipia.order.common.exception.ApiResponse;
import com.ipia.order.common.exception.order.OrderHandler;
import com.ipia.order.common.exception.order.status.OrderErrorStatus;
import com.ipia.order.common.exception.payment.status.PaymentErrorStatus;
import com.ipia.order.common.exception.payment.status.PaymentSuccessStatus;
import com.ipia.order.common.security.CurrentUser;
import com.ipia.order.order.domain.Order;
import com.ipia.order.order.service.OrderService;
import com.ipia.order.payment.service.PaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "결제", description = "결제 의도 생성, 승인, 취소, 검증 API")
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;

    public PaymentController(PaymentService paymentService, OrderService orderService) {
        this.paymentService = paymentService;
        this.orderService = orderService;
    }

    @Operation(summary = "결제 의도 생성", description = "주문에 대한 결제 의도를 생성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "의도 생성 성공",
                    content = @Content(schema = @Schema(implementation = IntentResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = PaymentErrorStatus.class, codes = {"INVALID_AMOUNT", "INVALID_SUCCESS_URL", "INVALID_FAIL_URL"})
    })
    @PostMapping("/intent")
    public ResponseEntity<ApiResponse<IntentResponse>> createIntent(
            @RequestBody IntentRequest request,
            @RequestHeader(name = "Idempotency-Key") String idempotencyKey,
            @AuthenticationPrincipal CurrentUser user) {
        
        // 주문 소유자 확인
        validateOrderOwnership(request.orderId(), user.getMemberId());
        
        String intentId = paymentService.createIntent(
                request.orderId(),
                request.amount(),
                request.successUrl(),
                request.failUrl(),
                idempotencyKey
        );
        return ApiResponse.onSuccess(PaymentSuccessStatus.INTENT_CREATED, new IntentResponse(intentId));
    }

    @Operation(summary = "결제 승인", description = "결제 키/의도/주문/금액을 검증하고 결제를 승인합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "승인 성공",
                    content = @Content(schema = @Schema(implementation = ApproveResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "중복 승인")
    })
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = PaymentErrorStatus.class, codes = {"PAYMENT_AMOUNT_MISMATCH", "PAYMENT_CANNOT_APPROVE", "DUPLICATE_PAYMENT_APPROVAL"})
    })
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<ApproveResponse>> approve(
            @RequestBody ApproveRequest request,
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
            @AuthenticationPrincipal CurrentUser user) {
        
        // 주문 소유자 확인
        validateOrderOwnership(request.orderId(), user.getMemberId());
        
        Long paymentId = paymentService.approve(
                request.intentId(),
                request.paymentKey(),
                request.orderId(),
                request.amount(),
                idempotencyKey
        );
        return ApiResponse.onSuccess(PaymentSuccessStatus.PAYMENT_APPROVED, new ApproveResponse(paymentId));
    }

    @Operation(summary = "결제 취소", description = "결제 키와 취소 금액, 사유로 결제를 취소합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "취소 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = PaymentErrorStatus.class, codes = {"PAYMENT_CANNOT_CANCEL", "CANCEL_AMOUNT_EXCEEDED", "INVALID_CANCEL_AMOUNT"})
    })
    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @RequestBody CancelRequest request,
            @AuthenticationPrincipal CurrentUser user) {
        
        // 결제를 통해 주문 소유자 확인 (PaymentService에서 주문 정보 조회 후 검증)
        // TODO: PaymentService에 주문 소유자 검증 로직 추가 필요
        
        paymentService.cancel(request.paymentKey(), request.cancelAmount(), request.reason());
        return ApiResponse.onSuccess(PaymentSuccessStatus.PAYMENT_CANCELED);
    }

    @Operation(summary = "결제 검증", description = "의도/결제키/주문/금액 일치 여부를 검증합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "검증 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = PaymentErrorStatus.class, codes = {"PAYMENT_AMOUNT_MISMATCH"})
    })
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Void>> verify(
            @RequestBody VerifyRequest request,
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
            @AuthenticationPrincipal CurrentUser user) {
        
        // 주문 소유자 확인
        validateOrderOwnership(request.orderId(), user.getMemberId());
        
        paymentService.verify(
                request.intentId(),
                request.paymentKey(),
                request.orderId(),
                request.amount(),
                idempotencyKey
        );
        return ApiResponse.onSuccess(PaymentSuccessStatus.PAYMENT_VERIFIED);
    }
    
    /**
     * 주문 소유자 확인 헬퍼 메서드
     */
    private void validateOrderOwnership(Long orderId, Long memberId) {
        Optional<Order> orderOptional = orderService.getOrder(orderId, memberId);
        if (orderOptional.isEmpty()) {
            throw new OrderHandler(OrderErrorStatus.ACCESS_DENIED);
        }
    }

    public record IntentRequest(long orderId, BigDecimal amount, String successUrl, String failUrl) {}
    public record ApproveRequest(String intentId, String paymentKey, long orderId, BigDecimal amount) {}
    public record CancelRequest(String paymentKey, BigDecimal cancelAmount, String reason) {}
    public record VerifyRequest(String intentId, String paymentKey, long orderId, BigDecimal amount) {}
    public record IntentResponse(String intentId) {}
    public record ApproveResponse(Long paymentId) {}
}


