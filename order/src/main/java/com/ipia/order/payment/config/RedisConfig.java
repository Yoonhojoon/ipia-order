package com.ipia.order.payment.config;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis 연결 상태 확인 및 공통 설정 관리
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisConfig {

    private final StringRedisTemplate redisTemplate;

    @PostConstruct
    public void checkRedisConnection() {
        try {
            // Redis 연결 테스트
            String testKey = "redis:health:check";
            redisTemplate.opsForValue().set(testKey, "ok", Duration.ofSeconds(10));
            String result = redisTemplate.opsForValue().get(testKey);
            redisTemplate.delete(testKey);

            if ("ok".equals(result)) {
                log.info("✅ Redis 연결 성공 - 모든 Redis 기반 서비스가 정상 동작합니다.");
                log.info("📊 Redis 활용 서비스: 멱등성 관리, 결제 의도 저장, 캐싱");
                log.info("🔧 Redis 기능: 빠른 응답 재사용, TTL 자동 만료, DB fallback 지원");
            } else {
                log.warn("⚠️ Redis 연결 불안정 - 예상치 못한 응답: {}. DB fallback으로 동작합니다.", result);
            }
        } catch (Exception e) {
            log.warn("⚠️ Redis 연결 실패 - DB fallback으로 동작합니다. Redis 설정을 확인하세요: spring.data.redis.host, spring.data.redis.port", e);
        }
    }
}
