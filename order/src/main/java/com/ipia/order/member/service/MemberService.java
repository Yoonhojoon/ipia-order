package com.ipia.order.member.service;

import com.ipia.order.member.domain.Member;
import java.util.List;
import java.util.Optional;

public interface MemberService {
    
    /**
     * 멤버 가입
     * @param name 회원 이름
     * @param email 이메일
     * @return 가입된 회원 정보
     */
    Member signup(String name, String email);

    /**
     * ID로 멤버 조회
     */
    Optional<Member> findById(Long id);

    /**
     * 이메일로 멤버 조회
     */
    Optional<Member> findByEmail(String email);

    /**
     * 모든 멤버 조회
     */
    List<Member> findAll();

    /**
     * 이름으로 멤버 조회
     */
    List<Member> findByName(String name);

    /**
     * 닉네임(이름) 변경
     */
    Member updateNickname(Long id, String newNickname);

    /**
     * 비밀번호 변경
     */
    void updatePassword(Long id, String currentPassword, String newPassword);

    /**
     * 회원 탈퇴
     */
    void withdraw(Long id);
}
