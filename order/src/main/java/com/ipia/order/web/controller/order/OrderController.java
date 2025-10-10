package com.ipia.order.web.controller.order;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ipia.order.common.exception.ApiErrorCodeExample;
import com.ipia.order.common.exception.ApiErrorCodeExamples;
import com.ipia.order.common.exception.ApiResponse;
import com.ipia.order.common.exception.order.OrderHandler;
import com.ipia.order.common.exception.order.status.OrderErrorStatus;
import com.ipia.order.common.exception.order.status.OrderSuccessStatus;
import com.ipia.order.common.security.CurrentUser;
import com.ipia.order.member.enums.MemberRole;
import com.ipia.order.order.domain.Order;
import com.ipia.order.order.service.OrderService;
import com.ipia.order.web.dto.request.order.CancelOrderRequest;
import com.ipia.order.web.dto.request.order.CreateOrderRequest;
import com.ipia.order.web.dto.response.order.OrderListResponse;
import com.ipia.order.web.dto.response.order.OrderResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
    @Operation(summary = "주문 생성", description = "새로운 주문을 생성합니다. 멱등성을 보장해, 동일한 멱등키가 요청이 들어오면 헤더에 x-idempotency 관련 헤더들이 추가됩니다.")
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
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Parameter(hidden = true) @AuthenticationPrincipal CurrentUser user) {
        
        // 토큰에서 사용자 ID 추출하여 본인만 주문 생성 가능
        Long memberId = user.getMemberId();
        
        Order order = orderService.createOrder(
            memberId, 
            request.getTotalAmount(), 
            idempotencyKey
        );
        
        OrderResponse response = OrderResponse.from(order);
        return ApiResponse.onSuccess(OrderSuccessStatus.ORDER_CREATED, response);
    }

    /**
     * 주문 조회 (단건)
     * GET /api/orders/{id}
     */
    @Operation(summary = "주문 단건 조회", description = "주문 ID를 통해 특정 주문 정보를 조회합니다. 주문 생성한 member가 아닌 경우 접근 권한 에러를 반환합니다.")
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
            @Parameter(description = "주문 ID", example = "1") @PathVariable("id") Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal CurrentUser user) {
       
        Long memberId = user.getMemberId();
        Optional<Order> orderOptional = orderService.getOrder(id, memberId);
        
        if (orderOptional.isEmpty()) {
            throw new OrderHandler(OrderErrorStatus.ORDER_NOT_FOUND);
        }
        
        OrderResponse response = OrderResponse.from(orderOptional.get());
        return ApiResponse.onSuccess(OrderSuccessStatus.ORDER_FOUND, response);
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
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(hidden = true) @AuthenticationPrincipal CurrentUser user) {
        
        // 일반 사용자는 본인 주문만 조회, 관리자는 모든 주문 조회 가능
        if (!isAdmin(user)) {
            memberId = user.getMemberId(); // 본인 ID로 강제 설정
        }
        
        OrderListResponse response = orderService.listOrders(memberId, status, page, size);
        return ApiResponse.onSuccess(OrderSuccessStatus.ORDERS_FOUND, response);
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
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Parameter(hidden = true) @AuthenticationPrincipal CurrentUser user) {
        
        // 주문 소유자 확인 (본인 주문만 취소 가능)
        Long memberId = user.getMemberId();
        Optional<Order> orderOptional = orderService.getOrder(id, memberId);
        
        if (orderOptional.isEmpty()) {
            throw new OrderHandler(OrderErrorStatus.ACCESS_DENIED);
        }
        
        Order order = orderService.cancelOrder(id, request.getReason());
        
        OrderResponse response = OrderResponse.from(order);
        return ApiResponse.onSuccess(OrderSuccessStatus.ORDER_CANCELED, response);
    }
    
    /**
     * 관리자 권한 확인 헬퍼 메서드
     */
    private boolean isAdmin(CurrentUser user) {
        return MemberRole.ADMIN.getCode().equals(user.getRole());
    }
}
