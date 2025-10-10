package com.ipia.order.web.controller.payment;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ipia.order.payment.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/intent")
    public ResponseEntity<IntentResponse> createIntent(@RequestBody IntentRequest request,
                                                       @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApproveResponse> approve(@RequestBody ApproveRequest request,
                                                   @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PostMapping("/cancel")
    public ResponseEntity<Void> cancel(@RequestBody CancelRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PostMapping("/verify")
    public ResponseEntity<Void> verify(@RequestBody VerifyRequest request,
                                       @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public record IntentRequest(long orderId, BigDecimal amount, String successUrl, String failUrl) {}
    public record ApproveRequest(String intentId, String paymentKey, long orderId, BigDecimal amount) {}
    public record CancelRequest(String paymentKey, BigDecimal cancelAmount, String reason) {}
    public record VerifyRequest(String intentId, String paymentKey, long orderId, BigDecimal amount) {}
    public record IntentResponse(String intentId) {}
    public record ApproveResponse(Long paymentId) {}
}


