package com.ipia.order.payment.service.external;

import java.math.BigDecimal;

public record TossConfirmResponse(
        String paymentKey,
        String orderId,
        BigDecimal approvedAmount
) {}


