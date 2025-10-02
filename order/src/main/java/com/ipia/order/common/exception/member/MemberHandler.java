package com.ipia.order.common.exception.member;


import com.ipia.order.common.exception.general.GeneralException;
import com.ipia.order.common.exception.member.status.MemberErrorStatus;

public class MemberHandler extends GeneralException {
    public MemberHandler(MemberErrorStatus status) {
        super(status);
    }
}
