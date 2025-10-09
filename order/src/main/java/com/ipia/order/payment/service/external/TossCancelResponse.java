package com.ipia.order.payment.service.external;

import java.math.BigDecimal;

public record TossCancelResponse(
        String paymentKey,
        BigDecimal cancelAmount,
        String status
) {}


