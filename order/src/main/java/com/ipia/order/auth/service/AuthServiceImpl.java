package com.ipia.order.auth.service;

import com.ipia.order.common.exception.auth.AuthHandler;
import com.ipia.order.common.exception.auth.status.AuthErrorStatus;
import com.ipia.order.common.util.PasswordEncoderUtil;
import com.ipia.order.common.util.JwtUtil;
import com.ipia.order.member.domain.Member;
import com.ipia.order.web.dto.response.auth.LoginResponse;
import com.ipia.order.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoderUtil passwordEncoder;
    private final JwtUtil jwtUtil;


    @Override
    public void logout(String token) {
        log.info("[Auth] 로그아웃 요청");
        // 1. 토큰 유효성 검증 (만료 여부 포함)
        try {
            jwtUtil.validateToken(token);
            log.debug("[Auth] 로그아웃 토큰 검증 완료");
        } catch (Exception e) {
            log.warn("[Auth] 유효하지 않은 로그아웃 토큰: {}", e.getMessage());
            throw new AuthHandler(AuthErrorStatus.INVALID_TOKEN);
        }

        // 2. 토큰 타입 확인 (ACCESS 토큰인지 확인)
        String tokenType = jwtUtil.getTokenType(token);
        if (!"ACCESS".equals(tokenType)) {
            log.warn("[Auth] ACCESS 토큰이 아님: {}", tokenType);
            throw new AuthHandler(AuthErrorStatus.INVALID_TOKEN);
        }

        // 4. 로그아웃 처리 (현재는 토큰 검증만 수행, 향후 Redis 블랙리스트 추가 예정)
        log.info("[Auth] 로그아웃 성공");
        // TODO: Redis 블랙리스트에 토큰 추가
        // redisTemplate.opsForValue().set("blacklist:" + token, "true", jwtUtil.getExpirationFromToken(token));
    }

    @Override
    @Transactional
    public Member register(String name, String email, String password) {
        log.info("[Auth] 회원가입 요청: email={}", email);
        // 이메일 중복 체크
        if (memberRepository.existsByEmail(email)) {
            log.warn("[Auth] 회원가입 실패: 중복 이메일 email={}", email);
            throw new AuthHandler(AuthErrorStatus.MEMBER_ALREADY_EXISTS);
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(password);
        log.debug("[Auth] 비밀번호 암호화 완료: email={}", email);

        // 회원 생성 및 저장
        Member member = Member.builder()
                .name(name)
                .email(email)
                .password(encodedPassword)
                .build();


        Member saved = memberRepository.save(member);
        log.info("[Auth] 회원가입 성공: memberId={}, email={}", saved.getId(), email);
        return saved;
    }

    @Override
    public LoginResponse login(String email, String password) {
        log.info("[Auth] 로그인 요청: email={}", email);
        // 1. 이메일로 회원 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new AuthHandler(AuthErrorStatus.MEMBER_NOT_FOUND));

        // 2. 회원 활성화 상태 확인
        if (!member.isActive()) {
            log.warn("[Auth] 비활성 회원 로그인 시도: memberId={}", member.getId());
            throw new AuthHandler(AuthErrorStatus.INACTIVE_MEMBER);
        }

        // 3. 비밀번호 검증
        if (!member.checkPassword(password, passwordEncoder)) {
            log.warn("[Auth] 로그인 실패: 비밀번호 불일치 memberId={}", member.getId());
            throw new AuthHandler(AuthErrorStatus.LOGIN_FAILED);
        }

        // 4. JWT 토큰 생성 (Access / Refresh)
        String accessToken = jwtUtil.generateAccessToken(
                member.getId(),
                member.getEmail(),
                member.getRole().getCode()
        );
        String refreshToken = jwtUtil.generateRefreshToken(member.getId());
        log.debug("[Auth] 토큰 생성 완료: memberId={}", member.getId());

        // 5. 응답 DTO 구성
        LoginResponse response = new LoginResponse(
                accessToken,
                refreshToken,
                member.getId(),
                member.getEmail(),
                member.getRole().getCode()
        );
        log.info("[Auth] 로그인 성공: memberId={}", member.getId());
        return response;
    }

    @Override
    public LoginResponse refresh(String refreshToken) {
        log.info("[Auth] 토큰 갱신 요청");
        // 1) 토큰 유효성 검사에서 발생하는 예외만 INVALID_TOKEN으로 매핑
        try {
            jwtUtil.validateToken(refreshToken);
            log.debug("[Auth] 리프레시 토큰 검증 완료");
        } catch (Exception e) {
            log.warn("[Auth] 유효하지 않은 리프레시 토큰: {}", e.getMessage());
            throw new AuthHandler(AuthErrorStatus.INVALID_TOKEN);
        }

        // 2) 토큰 타입 체크
        String tokenType = jwtUtil.getTokenType(refreshToken);
        if (!"REFRESH".equals(tokenType)) {
            log.warn("[Auth] REFRESH 토큰이 아님: {}", tokenType);
            throw new AuthHandler(AuthErrorStatus.INVALID_TOKEN);
        }

        // 3) 사용자 조회 및 상태 확인
        Long memberId = jwtUtil.getUserIdFromToken(refreshToken);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthHandler(AuthErrorStatus.MEMBER_NOT_FOUND));
        if (!member.isActive()) {
            log.warn("[Auth] 비활성 회원의 토큰 갱신 시도: memberId={}", member.getId());
            throw new AuthHandler(AuthErrorStatus.INACTIVE_MEMBER);
        }

        // 4) 새 토큰 발급
        String newAccessToken = jwtUtil.generateAccessToken(
                member.getId(),
                member.getEmail(),
                member.getRole().getCode()
        );
        String newRefreshToken = jwtUtil.generateRefreshToken(memberId);
        log.debug("[Auth] 새 토큰 생성 완료: memberId={}", member.getId());

        // 5) 응답 반환
        LoginResponse response = new LoginResponse(
                newAccessToken,
                newRefreshToken,
                member.getId(),
                member.getEmail(),
                member.getRole().getCode()
        );
        log.info("[Auth] 토큰 갱신 성공: memberId={}", member.getId());
        return response;
    }
}
