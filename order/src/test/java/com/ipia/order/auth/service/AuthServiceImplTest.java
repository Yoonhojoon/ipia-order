package com.ipia.order.auth.service;

import com.ipia.order.auth.service.AuthService;
import com.ipia.order.common.exception.auth.AuthHandler;
import com.ipia.order.common.exception.auth.status.AuthErrorStatus;
import com.ipia.order.common.util.PasswordEncoderUtil;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService 테스트")
class AuthServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoderUtil passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @Nested
    @DisplayName("로그인 테스트")
    class LoginTest {

        @Test
        @DisplayName("정상적인 이메일과 비밀번호로 로그인 성공")
        void 로그인_성공() {
            // Given
            String email = "test@example.com";
            String password = "password123";

            // When & Then - Red 테스트: 구현되지 않았으므로 UnsupportedOperationException 발생 예상
            assertThatThrownBy(() -> authService.login(email, password))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("Not implemented yet");
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 실패")
        void 존재하지_않는_이메일_로그인_실패() {
            // Given
            String email = "nonexistent@example.com";
            String password = "password123";

            // When & Then - Red 테스트: 구현되지 않았으므로 UnsupportedOperationException 발생 예상
            assertThatThrownBy(() -> authService.login(email, password))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("Not implemented yet");
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인 실패")
        void 잘못된_비밀번호_로그인_실패() {
            // Given
            String email = "test@example.com";
            String password = "wrongpassword";

            // When & Then - Red 테스트: 구현되지 않았으므로 UnsupportedOperationException 발생 예상
            assertThatThrownBy(() -> authService.login(email, password))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("Not implemented yet");
        }

        @Test
        @DisplayName("비활성화된 회원으로 로그인 실패")
        void 비활성화된_회원_로그인_실패() {
            // Given
            String email = "test@example.com";
            String password = "password123";

            // When & Then - Red 테스트: 구현되지 않았으므로 UnsupportedOperationException 발생 예상
            assertThatThrownBy(() -> authService.login(email, password))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("Not implemented yet");
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

            // When & Then - Red 테스트: 구현되지 않았으므로 UnsupportedOperationException 발생 예상
            assertThatThrownBy(() -> authService.logout(token))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("Not implemented yet");
        }

        @Test
        @DisplayName("유효하지 않은 토큰으로 로그아웃 실패")
        void 유효하지_않은_토큰_로그아웃_실패() {
            // Given
            String invalidToken = "invalid-token";

            // When & Then - Red 테스트: 구현되지 않았으므로 UnsupportedOperationException 발생 예상
            assertThatThrownBy(() -> authService.logout(invalidToken))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("Not implemented yet");
        }

        @Test
        @DisplayName("만료된 토큰으로 로그아웃 실패")
        void 만료된_토큰_로그아웃_실패() {
            // Given
            String expiredToken = "expired-jwt-token";

            // When & Then - Red 테스트: 구현되지 않았으므로 UnsupportedOperationException 발생 예상
            assertThatThrownBy(() -> authService.logout(expiredToken))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("Not implemented yet");
        }
    }
}