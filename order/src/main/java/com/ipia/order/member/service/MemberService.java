package com.ipia.order.member.service;

import com.ipia.order.member.domain.Member;

public interface MemberService {
    
    /**
     * 멤버 가입
     * @param name 회원 이름
     * @param email 이메일
     * @return 가입된 회원 정보
     */
    Member signup(String name, String email);
}
