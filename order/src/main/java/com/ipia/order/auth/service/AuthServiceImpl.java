package com.ipia.order.auth.service;

import com.ipia.order.common.exception.auth.AuthHandler;
import com.ipia.order.common.exception.auth.status.AuthErrorStatus;
import com.ipia.order.common.util.PasswordEncoderUtil;
import com.ipia.order.member.domain.Member;
import com.ipia.order.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoderUtil passwordEncoder;

    @Override
    public String login(String email, String password) {
        // TODO: 구현 예정
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void logout(String token) {
        // TODO: 구현 예정
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
