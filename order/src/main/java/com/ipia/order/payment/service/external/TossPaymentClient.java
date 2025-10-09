package com.ipia.order.payment.service.external;

import java.math.BigDecimal;

public interface TossPaymentClient {

    TossConfirmResponse confirm(String paymentKey, String orderId, BigDecimal amount);

    TossCancelResponse cancel(String paymentKey, BigDecimal cancelAmount, String reason);
}


