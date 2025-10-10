package com.ipia.order.common.config;


import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableCaching
@Slf4j
public class RedisConfig {
    // Redis CacheManager는 Spring Boot 자동구성 사용하되,
    // 값 직렬화와 TTL은 RedisCacheConfiguration 빈으로 명시 지정
    
    private final StringRedisTemplate stringRedisTemplate;
    
    public RedisConfig(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    @PostConstruct
    public void checkRedisConnection() {
        try {
            // Redis 연결 테스트
            String testKey = "redis:health:check";
            stringRedisTemplate.opsForValue().set(testKey, "ok", Duration.ofSeconds(10));
            String result = stringRedisTemplate.opsForValue().get(testKey);
            stringRedisTemplate.delete(testKey);

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


