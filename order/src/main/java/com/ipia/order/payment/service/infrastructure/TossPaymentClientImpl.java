package com.ipia.order.payment.service.infrastructure;

import java.math.BigDecimal;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipia.order.common.exception.payment.PaymentHandler;
import com.ipia.order.common.exception.payment.status.PaymentErrorStatus;
import com.ipia.order.payment.service.external.TossCancelResponse;
import com.ipia.order.payment.service.external.TossConfirmResponse;
import com.ipia.order.payment.service.external.TossPaymentClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * WebClient 기반 Toss Payments API 클라이언트 구현체
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TossPaymentClientImpl implements TossPaymentClient {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${toss.api.base-url:https://api.tosspayments.com}")
    private String baseUrl;

    @Value("${toss.api.secret-key}")
    private String secretKey;

    @Value("${toss.api.timeout-seconds:30}")
    private int timeoutSeconds;

    @Value("${toss.api.retry-attempts:3}")
    private int retryAttempts;

    @Override
    public TossConfirmResponse confirm(String paymentKey, String orderId, BigDecimal amount) {
        log.info("Toss 결제 승인 요청: paymentKey={}, orderId={}, amount={}", paymentKey, orderId, amount);
        
        WebClient webClient = createWebClient();
        
        TossConfirmRequest request = new TossConfirmRequest(paymentKey, orderId, amount);
        
        try {
            TossConfirmResponse response = webClient.post()
                    .uri("/v1/payments/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(TossConfirmResponse.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .retry(retryAttempts)
                    .block();
            
            log.info("Toss 결제 승인 성공: paymentKey={}, orderId={}", paymentKey, orderId);
            return response;
            
        } catch (WebClientResponseException e) {
            log.error("Toss 결제 승인 실패: paymentKey={}, status={}, body={}", 
                    paymentKey, e.getStatusCode(), e.getResponseBodyAsString());
            throw mapTossException(e, "결제 승인");
        } catch (Exception e) {
            log.error("Toss 결제 승인 네트워크 오류: paymentKey={}", paymentKey, e);
            throw new PaymentHandler(PaymentErrorStatus.TOSS_NETWORK_ERROR);
        }
    }

    @Override
    public TossCancelResponse cancel(String paymentKey, BigDecimal cancelAmount, String reason) {
        log.info("Toss 결제 취소 요청: paymentKey={}, cancelAmount={}, reason={}", paymentKey, cancelAmount, reason);
        
        WebClient webClient = createWebClient();
        
        TossCancelRequest request = new TossCancelRequest(cancelAmount, reason);
        
        try {
            TossCancelResponse response = webClient.post()
                    .uri("/v1/payments/{paymentKey}/cancel", paymentKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(TossCancelResponse.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .retry(retryAttempts)
                    .block();
            
            log.info("Toss 결제 취소 성공: paymentKey={}", paymentKey);
            return response;
            
        } catch (WebClientResponseException e) {
            log.error("Toss 결제 취소 실패: paymentKey={}, status={}, body={}", 
                    paymentKey, e.getStatusCode(), e.getResponseBodyAsString());
            throw mapTossException(e, "결제 취소");
        } catch (Exception e) {
            log.error("Toss 결제 취소 네트워크 오류: paymentKey={}", paymentKey, e);
            throw new PaymentHandler(PaymentErrorStatus.TOSS_NETWORK_ERROR);
        }
    }

    private WebClient createWebClient() {
        return webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Basic " + java.util.Base64.getEncoder()
                        .encodeToString((secretKey + ":").getBytes()))
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    private PaymentHandler mapTossException(WebClientResponseException e, String operation) {
        int statusCode = e.getStatusCode().value();
        
        if (statusCode >= 400 && statusCode < 500) {
            log.warn("Toss API 클라이언트 오류: {} - status={}, body={}", operation, statusCode, e.getResponseBodyAsString());
            return new PaymentHandler(PaymentErrorStatus.TOSS_API_ERROR);
        } else if (statusCode >= 500) {
            log.error("Toss API 서버 오류: {} - status={}, body={}", operation, statusCode, e.getResponseBodyAsString());
            return new PaymentHandler(PaymentErrorStatus.TOSS_API_ERROR);
        } else {
            log.error("Toss API 알 수 없는 오류: {} - status={}, body={}", operation, statusCode, e.getResponseBodyAsString());
            return new PaymentHandler(PaymentErrorStatus.TOSS_API_ERROR);
        }
    }

    /**
     * Toss 결제 승인 요청 DTO
     */
    public record TossConfirmRequest(
            String paymentKey,
            String orderId,
            BigDecimal amount
    ) {}

    /**
     * Toss 결제 취소 요청 DTO
     */
    public record TossCancelRequest(
            BigDecimal cancelAmount,
            String reason
    ) {}
}
