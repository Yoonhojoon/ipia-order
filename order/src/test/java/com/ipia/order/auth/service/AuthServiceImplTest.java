package com.ipia.order.auth.service;

import com.ipia.order.common.exception.auth.AuthHandler;
import com.ipia.order.common.exception.auth.JwtExceptionHandler;
import com.ipia.order.common.exception.auth.status.AuthErrorStatus;
import com.ipia.order.common.util.PasswordEncoderUtil;
import com.ipia.order.common.util.JwtUtil;
import com.ipia.order.member.enums.MemberRole;
import com.ipia.order.member.domain.Member;
import com.ipia.order.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServceImpl 테스트")
class AuthServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoderUtil passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    @Nested
    @DisplayName("로그인/토큰 발급 테스트")
    class LoginTest {

        @Test
        @DisplayName("정상적인 이메일과 비밀번호로 로그인/토큰 발급 성공")
        void 로그인_성공() {
            // Given
            String email = "test@example.com";
            String password = "password123";
            String encodedPassword = "encodedPassword";
            String expectedAccess = "access-token";
            String expectedRefresh = "refresh-token";
            
            Member member = Member.createTestMember(1L, "홍길동", email, encodedPassword, MemberRole.USER);
            
            given(memberRepository.findByEmail(email)).willReturn(Optional.of(member));
            given(passwordEncoder.matches(password, encodedPassword)).willReturn(true);
            given(jwtUtil.generateAccessToken(1L, email, "USER")).willReturn(expectedAccess);
            given(jwtUtil.generateRefreshToken(1L)).willReturn(expectedRefresh);

            // When
            var result = authService.login(email, password);

            // Then
            assertThat(result.getAccessToken()).isEqualTo(expectedAccess);
            assertThat(result.getRefreshToken()).isEqualTo(expectedRefresh);
            assertThat(result.getEmail()).isEqualTo(email);
            then(memberRepository).should(times(1)).findByEmail(email);
            then(jwtUtil).should(times(1)).generateAccessToken(1L, email, "USER");
            then(jwtUtil).should(times(1)).generateRefreshToken(1L);
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 실패")
        void 존재하지_않는_이메일_로그인_실패() {
            // Given
            String email = "nonexistent@example.com";
            String password = "password123";

            given(memberRepository.findByEmail(email)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> authService.login(email, password))
                .isInstanceOf(AuthHandler.class)
                .hasFieldOrPropertyWithValue("status", AuthErrorStatus.MEMBER_NOT_FOUND);
                
            then(memberRepository).should(times(1)).findByEmail(email);
            then(jwtUtil).should(never()).generateAccessToken(anyLong(), anyString(), anyString());
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인 실패")
        void 잘못된_비밀번호_로그인_실패() {
            // Given
            String email = "test@example.com";
            String password = "wrongpassword";
            String encodedPassword = "encodedPassword";
            
            Member member = Member.createTestMember(1L, "홍길동", email, encodedPassword, MemberRole.USER);
            
            given(memberRepository.findByEmail(email)).willReturn(Optional.of(member));
            given(passwordEncoder.matches(password, encodedPassword)).willReturn(false);

            // When & Then
            assertThatThrownBy(() -> authService.login(email, password))
                .isInstanceOf(AuthHandler.class)
                .hasFieldOrPropertyWithValue("status", AuthErrorStatus.LOGIN_FAILED);
                
            then(memberRepository).should(times(1)).findByEmail(email);
            then(jwtUtil).should(never()).generateAccessToken(anyLong(), anyString(), anyString());
        }

        @Test
        @DisplayName("비활성화된 회원으로 로그인 실패")
        void 비활성화된_회원_로그인_실패() {
            // Given
            String email = "test@example.com";
            String password = "password123";
            String encodedPassword = "encodedPassword";
            
            Member member = Member.createTestMember(1L, "홍길동", email, encodedPassword, MemberRole.USER);
            member.deactivate(); // 비활성화
            
            given(memberRepository.findByEmail(email)).willReturn(Optional.of(member));

            // When & Then
            assertThatThrownBy(() -> authService.login(email, password))
                .isInstanceOf(AuthHandler.class)
                .hasFieldOrPropertyWithValue("status", AuthErrorStatus.INACTIVE_MEMBER);
                
            then(memberRepository).should(times(1)).findByEmail(email);
            then(jwtUtil).should(never()).generateAccessToken(anyLong(), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("로그아웃 테스트")
    class LogoutTest {

        @Test
        @DisplayName("유효한 토큰으로 로그아웃 성공")
        void 로그아웃_성공() {
            // Given
            String token = "valid-jwt-token";
            
            doNothing().when(jwtUtil).validateToken(token);
            given(jwtUtil.getTokenType(token)).willReturn("ACCESS");

            // When & Then - 예외가 발생하지 않아야 함
            assertThatCode(() -> authService.logout(token))
                .doesNotThrowAnyException();
                
            then(jwtUtil).should(times(1)).validateToken(token);
            then(jwtUtil).should(times(1)).getTokenType(token);
        }

        @Test
        @DisplayName("유효하지 않은 토큰으로 로그아웃 실패")
        void 유효하지_않은_토큰_로그아웃_실패() {
            // Given
            String invalidToken = "invalid-token";
            
            doThrow(new JwtExceptionHandler("Invalid token")).when(jwtUtil).validateToken(invalidToken);

            // When & Then
            assertThatThrownBy(() -> authService.logout(invalidToken))
                .isInstanceOf(AuthHandler.class)
                .hasFieldOrPropertyWithValue("status", AuthErrorStatus.INVALID_TOKEN);
                
            then(jwtUtil).should(times(1)).validateToken(invalidToken);
            then(jwtUtil).should(never()).getTokenType(anyString());
        }

        @Test
        @DisplayName("만료된 토큰으로 로그아웃 실패")
        void 만료된_토큰_로그아웃_실패() {
            // Given
            String expiredToken = "expired-jwt-token";
            
            doThrow(new JwtExceptionHandler("Token expired")).when(jwtUtil).validateToken(expiredToken);

            // When & Then
            assertThatThrownBy(() -> authService.logout(expiredToken))
                .isInstanceOf(AuthHandler.class)
                .hasFieldOrPropertyWithValue("status", AuthErrorStatus.INVALID_TOKEN);
                
            then(jwtUtil).should(times(1)).validateToken(expiredToken);
            then(jwtUtil).should(never()).getTokenType(anyString());
        }
    }

    @Nested
    @DisplayName("회원가입 테스트")
    class RegisterTest {

        @Test
        @DisplayName("정상적인 입력으로 회원가입 성공 (비밀번호 암호화 저장)")
        void 회원가입_성공_암호화저장() {
            // Given
            String name = "홍길동";
            String email = "test@example.com";
            String rawPassword = "Password123!";
            String encoded = "$2a$10$encodedPassword";

            given(memberRepository.existsByEmail(email)).willReturn(false);
            given(passwordEncoder.encode(rawPassword)).willReturn(encoded);
            given(memberRepository.save(any(Member.class)))
                    .willAnswer(inv -> inv.getArgument(0));

            // When
            Member result = authService.register(name, email, rawPassword);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo(name);
            assertThat(result.getEmail()).isEqualTo(email);
            assertThat(result.getPassword()).isEqualTo(encoded);
            assertThat(result.getPassword()).isNotEqualTo(rawPassword);
            then(memberRepository).should(times(1)).existsByEmail(email);
            then(passwordEncoder).should(times(1)).encode(rawPassword);
            then(memberRepository).should(times(1)).save(any(Member.class));
        }

        @Test
        @DisplayName("중복 이메일로 회원가입 실패")
        void 회원가입_실패_중복이메일() {
            // Given
            String name = "홍길동";
            String email = "dup@example.com";
            String rawPassword = "Password123!";

            given(memberRepository.existsByEmail(email)).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> authService.register(name, email, rawPassword))
                .isInstanceOf(AuthHandler.class)
                .hasFieldOrPropertyWithValue("status", AuthErrorStatus.MEMBER_ALREADY_EXISTS);

            then(memberRepository).should(times(1)).existsByEmail(email);
            then(passwordEncoder).should(never()).encode(anyString());
            then(memberRepository).should(never()).save(any(Member.class));
        }
    }

    @Nested
    @DisplayName("리프레시 토큰 재발급 테스트")
    class RefreshTest {

        @Test
        @DisplayName("유효한 리프레시 토큰으로 재발급 성공")
        void 리프레시_성공() {
            // Given
            String refreshToken = "valid-refresh";
            Member member = Member.createTestMember(1L, "홍길동", "test@example.com", "enc", MemberRole.USER);

            doNothing().when(jwtUtil).validateToken(refreshToken);
            given(jwtUtil.getTokenType(refreshToken)).willReturn("REFRESH");
            given(jwtUtil.getUserIdFromToken(refreshToken)).willReturn(1L);
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));
            given(jwtUtil.generateAccessToken(1L, "test@example.com", "USER")).willReturn("new-access");
            given(jwtUtil.generateRefreshToken(1L)).willReturn("new-refresh");

            // When
            var result = authService.refresh(refreshToken);

            // Then
            assertThat(result.getAccessToken()).isEqualTo("new-access");
            assertThat(result.getRefreshToken()).isEqualTo("new-refresh");
            assertThat(result.getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("무효 토큰이면 INVALID_TOKEN 예외")
        void 리프레시_실패_무효토큰() {
            // Given
            String refreshToken = "invalid";
            doThrow(new JwtExceptionHandler("invalid")).when(jwtUtil).validateToken(refreshToken);

            // When & Then
            assertThatThrownBy(() -> authService.refresh(refreshToken))
                .isInstanceOf(AuthHandler.class)
                .hasFieldOrPropertyWithValue("status", AuthErrorStatus.INVALID_TOKEN);
        }

        @Test
        @DisplayName("토큰 타입이 REFRESH가 아니면 INVALID_TOKEN")
        void 리프레시_실패_토큰타입() {
            // Given
            String refreshToken = "access-token";
            doNothing().when(jwtUtil).validateToken(refreshToken);
            given(jwtUtil.getTokenType(refreshToken)).willReturn("ACCESS");

            // When & Then
            assertThatThrownBy(() -> authService.refresh(refreshToken))
                .isInstanceOf(AuthHandler.class)
                .hasFieldOrPropertyWithValue("status", AuthErrorStatus.INVALID_TOKEN);
        }

        @Test
        @DisplayName("비활성 회원이면 INACTIVE_MEMBER 예외")
        void 리프레시_실패_비활성회원() {
            // Given
            String refreshToken = "valid-refresh";
            Member member = Member.createTestMember(1L, "홍길동", "test@example.com", "enc", MemberRole.USER);
            member.deactivate();

            doNothing().when(jwtUtil).validateToken(refreshToken);
            given(jwtUtil.getTokenType(refreshToken)).willReturn("REFRESH");
            given(jwtUtil.getUserIdFromToken(refreshToken)).willReturn(1L);
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));

            // When & Then
            assertThatThrownBy(() -> authService.refresh(refreshToken))
                .isInstanceOf(AuthHandler.class)
                .hasFieldOrPropertyWithValue("status", AuthErrorStatus.INACTIVE_MEMBER);
        }

        @Test
        @DisplayName("회원이 없으면 MEMBER_NOT_FOUND 예외")
        void 리프레시_실패_회원없음() {
            // Given
            String refreshToken = "valid-refresh";
            doNothing().when(jwtUtil).validateToken(refreshToken);
            given(jwtUtil.getTokenType(refreshToken)).willReturn("REFRESH");
            given(jwtUtil.getUserIdFromToken(refreshToken)).willReturn(1L);
            given(memberRepository.findById(1L)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> authService.refresh(refreshToken))
                .isInstanceOf(AuthHandler.class)
                .hasFieldOrPropertyWithValue("status", AuthErrorStatus.MEMBER_NOT_FOUND);
        }
    }
}