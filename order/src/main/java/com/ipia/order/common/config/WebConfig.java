package com.ipia.order.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * Web MVC 설정
 * CORS 설정을 포함한 웹 관련 설정을 담당
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final Environment environment;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 현재 활성 프로파일 확인
        String[] activeProfiles = environment.getActiveProfiles();
        boolean isDevProfile = Arrays.asList(activeProfiles).contains("dev");
        boolean isProdProfile = Arrays.asList(activeProfiles).contains("prod");
        
        log.info("현재 활성 프로파일: {}", Arrays.toString(activeProfiles));
        
        if (isDevProfile) {
            configureCorsForDev(registry);
        } else if (isProdProfile) {
            configureCorsForProd(registry);
        } else {
            // 기본 설정 (기본 프로파일)
            configureCorsForDefault(registry);
        }
    }

    /**
     * 개발 환경 CORS 설정
     */
    private void configureCorsForDev(CorsRegistry registry) {
        log.info("개발 환경 CORS 설정 적용");
        
        registry.addMapping("/**")
                .allowedOrigins(
                    "http://localhost:3000",
                    "http://localhost:8080",
                    "http://127.0.0.1:3000",
                    "http://127.0.0.1:8080",
                    "http://localhost:5173"  // Vite 개발 서버
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
        
        log.info("개발 환경 CORS 설정 완료 - allowCredentials: true");
    }

    /**
     * 프로덕션 환경 CORS 설정
     */
    private void configureCorsForProd(CorsRegistry registry) {
        log.info("프로덕션 환경 CORS 설정 적용");
        
        // 환경변수에서 허용할 Origin 목록 가져오기
        String allowedOrigins = environment.getProperty("CORS_ALLOWED_ORIGINS", 
            "https://yourdomain.com,https://www.yourdomain.com");
        
        String[] origins = allowedOrigins.split(",");
        // 공백 제거
        for (int i = 0; i < origins.length; i++) {
            origins[i] = origins[i].trim();
        }
        
        registry.addMapping("/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders(
                    "Authorization",
                    "Content-Type",
                    "X-Requested-With",
                    "Accept",
                    "Origin"
                )
                .allowCredentials(true)
                .maxAge(3600);
        
        log.info("프로덕션 환경 CORS 설정 완료 - allowedOrigins: {}, allowCredentials: true", 
                Arrays.toString(origins));
    }

    /**
     * 기본 환경 CORS 설정 (안전한 기본값)
     */
    private void configureCorsForDefault(CorsRegistry registry) {
        log.warn("기본 CORS 설정 적용 - 보안을 위해 제한적 설정 사용");
        
        registry.addMapping("/**")
                .allowedOrigins("https://localhost:8080")  // 안전한 기본값
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type")
                .allowCredentials(true)
                .maxAge(3600);
        
        log.info("기본 환경 CORS 설정 완료");
    }
}
