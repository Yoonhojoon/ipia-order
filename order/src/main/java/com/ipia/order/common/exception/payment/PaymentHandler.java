package com.ipia.order.common.exception.payment;

import com.ipia.order.common.exception.general.GeneralException;
import com.ipia.order.common.exception.payment.status.PaymentErrorStatus;

public class PaymentHandler extends GeneralException {
    public PaymentHandler(PaymentErrorStatus status) {
        super(status);
    }
}
