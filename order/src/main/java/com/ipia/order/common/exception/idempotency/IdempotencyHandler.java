package com.ipia.order.common.exception.idempotency;


import com.ipia.order.common.exception.general.GeneralException;
import com.ipia.order.common.exception.general.status.ErrorResponse;

public class IdempotencyHandler extends GeneralException {
    public IdempotencyHandler(ErrorResponse status) {
        super(status);
    }
}


