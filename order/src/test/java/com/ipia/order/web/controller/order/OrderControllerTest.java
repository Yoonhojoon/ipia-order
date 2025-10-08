package com.ipia.order.web.controller.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipia.order.common.exception.order.OrderHandler;
import com.ipia.order.common.exception.order.status.OrderErrorStatus;
import com.ipia.order.common.util.JwtUtil;
import com.ipia.order.order.domain.Order;
import com.ipia.order.order.enums.OrderStatus;
import com.ipia.order.order.service.OrderService;
import com.ipia.order.web.dto.request.order.CancelOrderRequest;
import com.ipia.order.web.dto.request.order.CreateOrderRequest;
import org.mockito.Mockito;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.List;
import java.util.Optional;

/**
 * OrderController MockMvc 테스트
 */
@WebMvcTest(value = OrderController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        // 공통 Mock 설정
        Order mockOrder = createMockOrder(1L, 1L, 10000L, OrderStatus.CREATED);
        Order mockOrder2 = createMockOrder(2L, 1L, 20000L, OrderStatus.PENDING);
        
        // 주문 생성 Mock
        Mockito.when(orderService.createOrder(Mockito.eq(1L), Mockito.eq(10000L), Mockito.any()))
                .thenReturn(mockOrder);
        Mockito.when(orderService.createOrder(Mockito.eq(999L), Mockito.eq(10000L), Mockito.any()))
                .thenThrow(new OrderHandler(OrderErrorStatus.MEMBER_NOT_FOUND));
        Mockito.when(orderService.createOrder(Mockito.eq(1L), Mockito.eq(-1000L), Mockito.any()))
                .thenThrow(new OrderHandler(OrderErrorStatus.INVALID_AMOUNT));

        // 주문 조회 Mock
        Mockito.when(orderService.getOrder(Mockito.eq(1L))).thenReturn(Optional.of(mockOrder));
        Mockito.when(orderService.getOrder(Mockito.eq(999L))).thenReturn(Optional.empty());

        // 주문 목록 조회 Mock
        Mockito.when(orderService.listOrders(Mockito.eq(1L), Mockito.isNull(), Mockito.eq(0), Mockito.eq(10)))
                .thenReturn(List.of(mockOrder, mockOrder2));
        Mockito.when(orderService.listOrders(Mockito.isNull(), Mockito.eq(OrderStatus.PENDING), Mockito.eq(0), Mockito.eq(10)))
                .thenReturn(List.of(mockOrder2));

        // 주문 취소 Mock
        Order canceledOrder = createMockOrder(1L, 1L, 10000L, OrderStatus.CANCELED);
        Mockito.when(orderService.cancelOrder(Mockito.eq(1L), Mockito.any()))
                .thenReturn(canceledOrder);
        Mockito.when(orderService.cancelOrder(Mockito.eq(999L), Mockito.any()))
                .thenThrow(new OrderHandler(OrderErrorStatus.ORDER_NOT_FOUND));
    }

    @Test
    @DisplayName("주문 생성 API 테스트 - 성공")
    void createOrder_Success() throws Exception {
        // Given
        CreateOrderRequest request = createCreateOrderRequest(1L, 10000L);
        String jsonRequest = createJsonRequest(request);

        // When & Then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "test-key-123")
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.memberId").value(1L))
                .andExpect(jsonPath("$.data.totalAmount").value(10000L))
                .andExpect(jsonPath("$.data.status").value("CREATED"));
    }

    @Test
    @DisplayName("주문 생성 API 테스트 - 실패 (존재하지 않는 회원)")
    void createOrder_Failure_MemberNotFound() throws Exception {
        // Given
        CreateOrderRequest request = createCreateOrderRequest(999L, 10000L);
        String jsonRequest = createJsonRequest(request);

        // When & Then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("주문 생성 API 테스트 - 실패 (잘못된 금액)")
    void createOrder_Failure_InvalidAmount() throws Exception {
        // Given
        CreateOrderRequest request = createCreateOrderRequest(1L, -1000L);
        String jsonRequest = createJsonRequest(request);

        // When & Then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("주문 조회 API 테스트 - 성공")
    void getOrder_Success() throws Exception {
        // Given
        Long orderId = 1L;

        // When & Then
        mockMvc.perform(get("/api/orders/{id}", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(orderId));
    }

    @Test
    @DisplayName("주문 조회 API 테스트 - 실패 (존재하지 않는 주문)")
    void getOrder_Failure_NotFound() throws Exception {
        // Given
        Long orderId = 999L;

        // When & Then
        mockMvc.perform(get("/api/orders/{id}", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("주문 목록 조회 API 테스트 - 성공 (회원 ID 필터)")
    void listOrders_Success_WithMemberId() throws Exception {
        // Given
        Long memberId = 1L;

        // When & Then
        mockMvc.perform(get("/api/orders")
                        .param("memberId", memberId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.orders").isArray())
                .andExpect(jsonPath("$.data.orders.length()").value(2));
    }

    @Test
    @DisplayName("주문 목록 조회 API 테스트 - 성공 (상태 필터)")
    void listOrders_Success_WithStatus() throws Exception {
        // Given
        String status = "PENDING";

        // When & Then
        mockMvc.perform(get("/api/orders")
                        .param("status", status)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.orders").isArray())
                .andExpect(jsonPath("$.data.orders.length()").value(1));
    }

    @Test
    @DisplayName("주문 취소 API 테스트 - 성공")
    void cancelOrder_Success() throws Exception {
        // Given
        Long orderId = 1L;
        CancelOrderRequest request = createCancelOrderRequest("고객 요청");
        String jsonRequest = createJsonRequest(request);

        // When & Then
        mockMvc.perform(post("/api/orders/{id}/cancel", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "cancel-key-123")
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(orderId))
                .andExpect(jsonPath("$.data.status").value("CANCELED"));
    }

    @Test
    @DisplayName("주문 취소 API 테스트 - 실패 (존재하지 않는 주문)")
    void cancelOrder_Failure_NotFound() throws Exception {
        // Given
        Long orderId = 999L;
        CancelOrderRequest request = createCancelOrderRequest("고객 요청");
        String jsonRequest = createJsonRequest(request);

        // When & Then
        mockMvc.perform(post("/api/orders/{id}/cancel", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    // === 헬퍼 메서드들 ===

    /**
     * JSON 요청 본문 생성
     */
    private String createJsonRequest(Object request) throws Exception {
        return objectMapper.writeValueAsString(request);
    }

    /**
     * 주문 생성 요청 DTO 생성
     */
    private CreateOrderRequest createCreateOrderRequest(Long memberId, Long totalAmount) {
        return CreateOrderRequest.builder()
                .memberId(memberId)
                .totalAmount(totalAmount)
                .build();
    }

    /**
     * 주문 취소 요청 DTO 생성
     */
    private CancelOrderRequest createCancelOrderRequest(String reason) {
        return CancelOrderRequest.builder()
                .reason(reason)
                .build();
    }

    /**
     * Mock Order 엔티티 생성
     */
    private Order createMockOrder(Long id, Long memberId, Long totalAmount, OrderStatus status) {
        return Order.createTestOrder(id, memberId, totalAmount, status);
    }
}
