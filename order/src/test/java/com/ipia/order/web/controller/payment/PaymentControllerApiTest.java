package com.ipia.order.web.controller.payment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.ipia.order.payment.service.PaymentService;

@WebMvcTest(controllers = com.ipia.order.web.controller.payment.PaymentController.class)
class PaymentControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Test
    @DisplayName("의도 생성 API: 성공시 intentId 반환")
    void createIntent_success() throws Exception {
        // given
        var reqJson = "{" +
                "\"orderId\":1," +
                "\"amount\":10000," +
                "\"successUrl\":\"https://success\"," +
                "\"failUrl\":\"https://fail\"" +
                "}";
        when(paymentService.createIntent(eq(1L), eq(new BigDecimal("10000")), eq("https://success"), eq("https://fail"), any()))
                .thenReturn("intent_123");

        // when/then
        mockMvc.perform(post("/api/payments/intent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqJson)
                        .header("Idempotency-Key", "idem-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intentId").value("intent_123"));

        verify(paymentService).createIntent(1L, new BigDecimal("10000"), "https://success", "https://fail", "idem-1");
    }

    @Test
    @DisplayName("결제 승인 API: 성공시 paymentId 반환")
    void approve_success() throws Exception {
        // given
        var reqJson = "{" +
                "\"intentId\":\"intent_123\"," +
                "\"paymentKey\":\"pay_abc\"," +
                "\"orderId\":1," +
                "\"amount\":10000" +
                "}";
        when(paymentService.approve(eq("intent_123"), eq("pay_abc"), eq(1L), eq(new BigDecimal("10000")), any()))
                .thenReturn(10L);

        // when/then
        mockMvc.perform(post("/api/payments/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqJson)
                        .header("Idempotency-Key", "idem-2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(10));

        verify(paymentService).approve("intent_123", "pay_abc", 1L, new BigDecimal("10000"), "idem-2");
    }

    @Test
    @DisplayName("결제 취소 API: 성공시 204 반환")
    void cancel_success() throws Exception {
        // given
        var reqJson = "{" +
                "\"paymentKey\":\"pay_abc\"," +
                "\"cancelAmount\":5000," +
                "\"reason\":\"user cancel\"" +
                "}";

        // when/then
        mockMvc.perform(post("/api/payments/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqJson))
                .andExpect(status().isNoContent());

        verify(paymentService).cancel("pay_abc", new BigDecimal("5000"), "user cancel");
    }

    @Test
    @DisplayName("결제 검증 API: 성공시 204 반환")
    void verify_success() throws Exception {
        // given
        var reqJson = "{" +
                "\"intentId\":\"intent_123\"," +
                "\"paymentKey\":\"pay_abc\"," +
                "\"orderId\":1," +
                "\"amount\":10000" +
                "}";

        // when/then
        mockMvc.perform(post("/api/payments/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqJson)
                        .header("Idempotency-Key", "idem-3"))
                .andExpect(status().isNoContent());

        verify(paymentService).verify("intent_123", "pay_abc", 1L, new BigDecimal("10000"), "idem-3");
    }
}


