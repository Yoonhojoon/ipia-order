package com.ipia.order.auth.service;

import com.ipia.order.common.exception.auth.AuthHandler;
import com.ipia.order.common.exception.auth.status.AuthErrorStatus;
import com.ipia.order.common.util.PasswordEncoderUtil;
import com.ipia.order.common.util.JwtUtil;
import com.ipia.order.member.domain.Member;
import com.ipia.order.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoderUtil passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public String login(String email, String password) {
        // 1. 이메일로 회원 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new AuthHandler(AuthErrorStatus.MEMBER_NOT_FOUND));

        // 2. 회원 활성화 상태 확인
        if (!member.isActive()) {
            throw new AuthHandler(AuthErrorStatus.INACTIVE_MEMBER);
        }

        // 3. 비밀번호 검증
        if (!member.checkPassword(password, passwordEncoder)) {
            throw new AuthHandler(AuthErrorStatus.LOGIN_FAILED);
        }

        // 4. JWT 토큰 생성 및 반환
        return jwtUtil.generateAccessToken(
                member.getId(),
                member.getEmail(),
                member.getRole().getCode()
        );
    }

    @Override
    public void logout(String token) {
        // 1. 토큰 유효성 검증 (만료 여부 포함)
        try {
            jwtUtil.validateToken(token);
        } catch (Exception e) {
            throw new AuthHandler(AuthErrorStatus.INVALID_TOKEN);
        }

        // 2. 토큰 타입 확인 (ACCESS 토큰인지 확인)
        String tokenType = jwtUtil.getTokenType(token);
        if (!"ACCESS".equals(tokenType)) {
            throw new AuthHandler(AuthErrorStatus.INVALID_TOKEN);
        }

        // 4. 로그아웃 처리 (현재는 토큰 검증만 수행, 향후 Redis 블랙리스트 추가 예정)
        // TODO: Redis 블랙리스트에 토큰 추가
        // redisTemplate.opsForValue().set("blacklist:" + token, "true", jwtUtil.getExpirationFromToken(token));
    }

    @Override
    @Transactional
    public Member register(String name, String email, String password) {
        // 이메일 중복 체크
        if (memberRepository.existsByEmail(email)) {
            throw new AuthHandler(AuthErrorStatus.MEMBER_ALREADY_EXISTS);
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(password);

        // 회원 생성 및 저장
        Member member = Member.builder()
                .name(name)
                .email(email)
                .password(encodedPassword)
                .build();

        return memberRepository.save(member);
    }
}
