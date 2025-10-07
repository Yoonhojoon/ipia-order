package com.ipia.order.common.exception.order;


import com.ipia.order.common.exception.general.GeneralException;
import com.ipia.order.common.exception.order.status.OrderErrorStatus;

public class OrderHandler extends GeneralException {
    public OrderHandler(OrderErrorStatus status) {
        super(status);
    }
}
