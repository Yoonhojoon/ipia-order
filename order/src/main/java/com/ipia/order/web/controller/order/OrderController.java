package com.ipia.order.web.controller.order;

import com.ipia.order.common.exception.ApiErrorCodeExample;
import com.ipia.order.common.exception.ApiErrorCodeExamples;
import com.ipia.order.common.exception.ApiResponse;
import com.ipia.order.common.exception.order.status.OrderErrorStatus;
import com.ipia.order.common.exception.order.status.OrderSuccessStatus;
import com.ipia.order.order.domain.Order;
import com.ipia.order.order.service.OrderService;
import com.ipia.order.web.dto.request.order.CreateOrderRequest;
import com.ipia.order.web.dto.request.order.CancelOrderRequest;
import com.ipia.order.web.dto.response.order.OrderResponse;
import com.ipia.order.web.dto.response.order.OrderListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 주문 관리 컨트롤러
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "주문 관리", description = "주문 생성, 조회, 취소 등의 주문 관리 API")
public class OrderController {

    private final OrderService orderService;

    /**
     * 주문 생성
     * POST /api/orders
     */
    @Operation(summary = "주문 생성", description = "새로운 주문을 생성합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "주문 생성 성공", 
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "멱등 키 중복")
    })
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = OrderErrorStatus.class, codes = {"MEMBER_NOT_FOUND", "INVALID_AMOUNT", "IDEMPOTENCY_CONFLICT"})
    })
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        // TODO: 구현 예정
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 주문 조회 (단건)
     * GET /api/orders/{id}
     */
    @Operation(summary = "주문 단건 조회", description = "주문 ID를 통해 특정 주문 정보를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "주문 조회 성공", 
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한 없음")
    })
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = OrderErrorStatus.class, codes = {"ORDER_NOT_FOUND", "ACCESS_DENIED"})
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @Parameter(description = "주문 ID", example = "1") @PathVariable("id") Long id) {
        // TODO: 구현 예정
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 주문 목록 조회
     * GET /api/orders
     */
    @Operation(summary = "주문 목록 조회", description = "필터 조건에 따라 주문 목록을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "주문 목록 조회 성공", 
                    content = @Content(schema = @Schema(implementation = OrderListResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = OrderErrorStatus.class, codes = {"INVALID_FILTER", "INVALID_PAGINATION"})
    })
    @GetMapping
    public ResponseEntity<ApiResponse<OrderListResponse>> listOrders(
            @Parameter(description = "회원 ID", example = "1") @RequestParam(value = "memberId", required = false) Long memberId,
            @Parameter(description = "주문 상태", example = "PENDING") @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "페이지 번호", example = "0") @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(value = "size", defaultValue = "10") int size) {
        // TODO: 구현 예정
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 주문 취소
     * POST /api/orders/{id}/cancel
     */
    @Operation(summary = "주문 취소", description = "기존 주문을 취소합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "주문 취소 성공", 
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "멱등 키 중복")
    })
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = OrderErrorStatus.class, codes = {"ORDER_NOT_FOUND", "INVALID_ORDER_STATE", "IDEMPOTENCY_CONFLICT"})
    })
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @Parameter(description = "주문 ID", example = "1") @PathVariable("id") Long id,
            @Valid @RequestBody CancelOrderRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        // TODO: 구현 예정
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
