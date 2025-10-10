package com.ipia.order.payment.service.infrastructure;

import java.math.BigDecimal;
import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipia.order.payment.service.port.PaymentIntentStore;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis 기반 PaymentIntent 저장소 구현체
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisPaymentIntentStore implements PaymentIntentStore {

    private static final String REDIS_KEY_PREFIX = "payment:intent:";
    
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void store(String intentId, Long orderId, BigDecimal amount, String successUrl, String failUrl, String idempotencyKey, long ttlSeconds) {
        try {
            String key = buildRedisKey(intentId);
            PaymentIntentData data = new PaymentIntentData(intentId, orderId, amount, successUrl, failUrl, idempotencyKey);
            String jsonData = objectMapper.writeValueAsString(data);
            
            redisTemplate.opsForValue().set(key, jsonData, Duration.ofSeconds(ttlSeconds));
            log.debug("PaymentIntent 저장 완료: intentId={}, orderId={}, amount={}, ttl={}초", intentId, orderId, amount, ttlSeconds);
            
        } catch (JsonProcessingException e) {
            log.error("PaymentIntent 저장 실패 - JSON 직렬화 오류: intentId={}", intentId, e);
            throw new RuntimeException("PaymentIntent 저장 실패", e);
        } catch (Exception e) {
            log.error("PaymentIntent 저장 실패: intentId={}", intentId, e);
            throw new RuntimeException("PaymentIntent 저장 실패", e);
        }
    }

    @Override
    public PaymentIntentData get(String intentId) {
        try {
            String key = buildRedisKey(intentId);
            String jsonData = redisTemplate.opsForValue().get(key);
            
            if (jsonData == null) {
                log.debug("PaymentIntent 조회 결과 없음: intentId={}", intentId);
                return null;
            }
            
            PaymentIntentData data = objectMapper.readValue(jsonData, PaymentIntentData.class);
            log.debug("PaymentIntent 조회 성공: intentId={}, orderId={}", intentId, data.orderId());
            return data;
            
        } catch (JsonProcessingException e) {
            log.error("PaymentIntent 조회 실패 - JSON 역직렬화 오류: intentId={}", intentId, e);
            return null;
        } catch (Exception e) {
            log.error("PaymentIntent 조회 실패: intentId={}", intentId, e);
            return null;
        }
    }

    @Override
    public void delete(String intentId) {
        try {
            String key = buildRedisKey(intentId);
            redisTemplate.delete(key);
            log.debug("PaymentIntent 삭제 완료: intentId={}", intentId);
            
        } catch (Exception e) {
            log.error("PaymentIntent 삭제 실패: intentId={}", intentId, e);
            throw new RuntimeException("PaymentIntent 삭제 실패", e);
        }
    }

    private String buildRedisKey(String intentId) {
        return REDIS_KEY_PREFIX + intentId;
    }
}
