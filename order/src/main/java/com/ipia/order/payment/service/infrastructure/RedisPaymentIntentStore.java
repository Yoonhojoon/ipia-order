package com.ipia.order.payment.service.infrastructure;

import java.math.BigDecimal;
import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipia.order.payment.domain.PaymentIntent;
import com.ipia.order.payment.repository.PaymentIntentRepository;
import com.ipia.order.payment.service.port.PaymentIntentStore;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis 기반 PaymentIntent 저장소 구현체 (DB Fallback 지원)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisPaymentIntentStore implements PaymentIntentStore {

    private static final String REDIS_KEY_PREFIX = "payment:intent:";
    
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final PaymentIntentRepository paymentIntentRepository;



    @Override
    public void store(String intentId, Long orderId, BigDecimal amount, String successUrl, String failUrl, String idempotencyKey, long ttlSeconds) {
        // Redis에 저장 시도
        try {
            String key = buildRedisKey(intentId);
            PaymentIntentData data = new PaymentIntentData(intentId, orderId, amount, successUrl, failUrl, idempotencyKey);
            String jsonData = objectMapper.writeValueAsString(data);
            
            redisTemplate.opsForValue().set(key, jsonData, Duration.ofSeconds(ttlSeconds));
            log.debug("✅ PaymentIntent Redis 저장 완료: intentId={}, orderId={}, amount={}, ttl={}초", intentId, orderId, amount, ttlSeconds);
            return;
            
        } catch (Exception e) {
            log.warn("⚠️ PaymentIntent Redis 저장 실패 - DB fallback으로 전환: intentId={}", intentId, e);
        }
        
        // Redis 실패 시 DB fallback
        try {
            PaymentIntent paymentIntent = PaymentIntent.create(intentId, orderId, amount, successUrl, failUrl, idempotencyKey, ttlSeconds);
            paymentIntentRepository.save(paymentIntent);
            log.info("✅ PaymentIntent DB fallback 저장 완료: intentId={}, orderId={}, amount={}, ttl={}초", intentId, orderId, amount, ttlSeconds);
            
        } catch (Exception e) {
            log.error("❌ PaymentIntent 저장 실패 (Redis + DB 모두 실패): intentId={}", intentId, e);
            throw new RuntimeException("PaymentIntent 저장 실패 - Redis와 DB 모두 사용 불가", e);
        }
    }

    @Override
    public PaymentIntentData get(String intentId) {
        // Redis에서 조회 시도
        try {
            String key = buildRedisKey(intentId);
            String jsonData = redisTemplate.opsForValue().get(key);
            
            if (jsonData != null) {
                PaymentIntentData data = objectMapper.readValue(jsonData, PaymentIntentData.class);
                log.debug("✅ PaymentIntent Redis 조회 성공: intentId={}, orderId={}", intentId, data.orderId());
                return data;
            }
            
        } catch (Exception e) {
            log.warn("⚠️ PaymentIntent Redis 조회 실패 - DB fallback으로 전환: intentId={}", intentId, e);
        }
        
        // Redis에서 조회 실패하거나 데이터가 없는 경우 DB fallback
        try {
            return paymentIntentRepository.findByIntentId(intentId)
                    .map(this::convertToPaymentIntentData)
                    .orElse(null);
                    
        } catch (Exception e) {
            log.error("❌ PaymentIntent 조회 실패 (Redis + DB 모두 실패): intentId={}", intentId, e);
            return null;
        }
    }

    @Override
    public void delete(String intentId) {
        // Redis에서 삭제 시도
        try {
            String key = buildRedisKey(intentId);
            redisTemplate.delete(key);
            log.debug("✅ PaymentIntent Redis 삭제 완료: intentId={}", intentId);
        } catch (Exception e) {
            log.warn("⚠️ PaymentIntent Redis 삭제 실패: intentId={}", intentId, e);
        }
        
        // DB에서도 삭제 시도 (Redis 실패 시에도 DB 정리)
        try {
            paymentIntentRepository.findByIntentId(intentId)
                    .ifPresent(paymentIntent -> {
                        paymentIntentRepository.delete(paymentIntent);
                        log.debug("✅ PaymentIntent DB 삭제 완료: intentId={}", intentId);
                    });
        } catch (Exception e) {
            log.warn("⚠️ PaymentIntent DB 삭제 실패: intentId={}", intentId, e);
        }
    }

    private String buildRedisKey(String intentId) {
        return REDIS_KEY_PREFIX + intentId;
    }
    
    private PaymentIntentData convertToPaymentIntentData(PaymentIntent paymentIntent) {
        // 만료된 데이터인지 확인
        if (paymentIntent.isExpired()) {
            log.debug("PaymentIntent 만료됨 - DB에서 삭제: intentId={}", paymentIntent.getIntentId());
            try {
                paymentIntentRepository.delete(paymentIntent);
            } catch (Exception e) {
                log.warn("만료된 PaymentIntent DB 삭제 실패: intentId={}", paymentIntent.getIntentId(), e);
            }
            return null;
        }
        
        log.debug("✅ PaymentIntent DB 조회 성공: intentId={}, orderId={}", paymentIntent.getIntentId(), paymentIntent.getOrderId());
        return new PaymentIntentData(
                paymentIntent.getIntentId(),
                paymentIntent.getOrderId(),
                paymentIntent.getAmount(),
                paymentIntent.getSuccessUrl(),
                paymentIntent.getFailUrl(),
                paymentIntent.getIdempotencyKey()
        );
    }
}
