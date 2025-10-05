package com.ipia.order.common.exception.auth;

import com.ipia.order.common.exception.general.GeneralException;
import com.ipia.order.common.exception.auth.status.AuthErrorStatus;

public class AuthHandler extends GeneralException {
    public AuthHandler(AuthErrorStatus status) {
        super(status);
    }
}
