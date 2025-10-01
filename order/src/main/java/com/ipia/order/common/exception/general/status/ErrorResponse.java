package com.ipia.order.common.exception.general.status;

import org.springframework.http.HttpStatus;

public interface ErrorResponse {
    HttpStatus getErrorStatus();
    String getCode();
    String getMessage();
}
