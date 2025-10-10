package com.ipia.order.payment.service.external;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.ipia.order.payment.config.TossProperties;
import com.ipia.order.common.exception.payment.PaymentHandler;
import com.ipia.order.common.exception.payment.status.PaymentErrorStatus;

/**
 * 기본 구현체: 실제 연동 전까지는 단순 에코 응답을 반환한다.
 * 운영 연동 시 이 구현을 교체하거나 내부 로직을 실제 API 호출로 대체한다.
 */
@Component
public class TossPaymentClientImpl implements TossPaymentClient {

    private final WebClient webClient;
    private final TossProperties tossProperties;

    public TossPaymentClientImpl(WebClient.Builder webClientBuilder, TossProperties tossProperties) {
        this.webClient = webClientBuilder.baseUrl(tossProperties.getBaseUrl()).build();
        this.tossProperties = tossProperties;
    }

    @Override
    public TossConfirmResponse confirm(String paymentKey, String orderId, BigDecimal amount) {
        if (!tossProperties.isEnableRealCall()) {
            return new TossConfirmResponse(paymentKey, orderId, amount);
        }

        try {
            var response = webClient.post()
                    .uri("/v1/payments/confirm")
                    .header("Authorization", buildBasicAuthHeader(tossProperties.getSecretKey()))
                    .header("Content-Type", "application/json")
                    .bodyValue(new ConfirmRequest(paymentKey, orderId, amount))
                    .retrieve()
                    .bodyToMono(ConfirmResponseBody.class)
                    .block();

            if (response == null) {
                throw new PaymentHandler(PaymentErrorStatus.TOSS_RESPONSE_PARSE_ERROR);
            }
            return new TossConfirmResponse(response.paymentKey, response.orderId, response.totalAmount);
        } catch (WebClientResponseException e) {
            throw new PaymentHandler(PaymentErrorStatus.TOSS_API_ERROR);
        } catch (Exception e) {
            throw new PaymentHandler(PaymentErrorStatus.TOSS_NETWORK_ERROR);
        }
    }

    @Override
    public TossCancelResponse cancel(String paymentKey, BigDecimal cancelAmount, String reason) {
        if (!tossProperties.isEnableRealCall()) {
            return new TossCancelResponse(paymentKey, cancelAmount, "CANCELED");
        }
        try {
            var response = webClient.post()
                    .uri("/v1/payments/" + paymentKey + "/cancel")
                    .header("Authorization", buildBasicAuthHeader(tossProperties.getSecretKey()))
                    .header("Content-Type", "application/json")
                    .bodyValue(new CancelRequest(cancelAmount, reason))
                    .retrieve()
                    .bodyToMono(CancelResponseBody.class)
                    .block();
            if (response == null) {
                throw new PaymentHandler(PaymentErrorStatus.TOSS_RESPONSE_PARSE_ERROR);
            }
            return new TossCancelResponse(paymentKey, response.cancelAmount, response.status);
        } catch (WebClientResponseException e) {
            throw new PaymentHandler(PaymentErrorStatus.TOSS_API_ERROR);
        } catch (Exception e) {
            throw new PaymentHandler(PaymentErrorStatus.TOSS_NETWORK_ERROR);
        }
    }

    private String buildBasicAuthHeader(String secretKey) {
        String token = secretKey + ":";
        String encoded = Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }

    private record ConfirmRequest(String paymentKey, String orderId, BigDecimal amount) {}
    private record ConfirmResponseBody(String paymentKey, String orderId, BigDecimal totalAmount) {}
    private record CancelRequest(BigDecimal cancelAmount, String cancelReason) {}
    private record CancelResponseBody(BigDecimal cancelAmount, String status) {}
}


