package com.ipia.order.common.config;

import com.ipia.order.common.filter.JwtAuthenticationFilter;
import com.ipia.order.common.filter.JwtExceptionHandlerFilter;
import com.ipia.order.common.exception.SecurityExceptionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtExceptionHandlerFilter jwtExceptionHandlerFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final SecurityExceptionHandler securityExceptionHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화 (JWT 사용 시 필요 없음)
            .csrf(AbstractHttpConfigurer::disable)
            
            // CORS 설정 (WebConfig에서 처리)
            .cors(cors -> {})
            
            // 세션 관리 정책 설정 (STATELESS로 JWT 사용)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 요청 권한 설정
            .authorizeHttpRequests(auth -> auth
                // 공개 엔드포인트
                .requestMatchers(
                    "/api/auth/login",
                    "/api/auth/signup",
                    "/h2-console/**",
                    "/actuator/**"
                ).permitAll()
                
                // 관리자 권한이 필요한 엔드포인트
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // 판매자 권한이 필요한 엔드포인트
                .requestMatchers("/api/seller/**").hasRole("SELLER")
                
                // 인증이 필요한 엔드포인트
                .requestMatchers("/api/**").authenticated()
                
                // 나머지 요청은 허용
                .anyRequest().permitAll()
            )
            
            // H2 콘솔을 위한 프레임 옵션 설정
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
            
            // 예외 처리 핸들러 설정
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(securityExceptionHandler)
                .accessDeniedHandler(securityExceptionHandler)
            )
            
            // JWT 필터 추가 (순서 중요: ExceptionHandler -> Authentication)
            .addFilterBefore(jwtExceptionHandlerFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
