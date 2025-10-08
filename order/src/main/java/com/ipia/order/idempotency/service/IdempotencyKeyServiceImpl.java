package com.ipia.order.idempotency.service;

import com.ipia.order.common.exception.idempotency.IdempotencyHandler;
import com.ipia.order.common.exception.idempotency.status.IdempotencyErrorStatus;
import com.ipia.order.idempotency.domain.IdempotencyKey;
import com.ipia.order.idempotency.repository.IdempotencyKeyRepository;
import lombok.RequiredArgsConstructor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.Map;
import java.util.concurrent.locks.LockSupport;
import com.ipia.order.idempotency.support.IdempotencyReplayContext;
import com.ipia.order.idempotency.support.IdempotencyReplayContextHolder;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class IdempotencyKeyServiceImpl implements IdempotencyKeyService {

    private static final String CACHE_NAME = "idemp";
    private static final String REDIS_NAMESPACE = "idemp:";
    private static final String REDIS_LOCK_NAMESPACE = "idemp:lock:";
    private static final Duration RESERVATION_TTL = Duration.ofMinutes(10);
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_RESPONSE = "response";
    private static final String FIELD_CREATED_AT = "created_at";
    private static final String FIELD_EXPIRES_AT = "expires_at";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_PENDING = "PENDING";

    private final IdempotencyKeyRepository repository;
    private final ObjectMapper objectMapper;
    
    // Redis 필수: 멱등성 서비스는 Redis가 반드시 필요
    private final StringRedisTemplate redisTemplate;

    @PostConstruct
    public void checkRedisConnection() {
        try {
            // Redis 연결 테스트
            String testKey = "idempotency:health:check";
            redisTemplate.opsForValue().set(testKey, "ok", Duration.ofSeconds(10));
            String result = redisTemplate.opsForValue().get(testKey);
            redisTemplate.delete(testKey);

            if ("ok".equals(result)) {
                log.info("✅ Redis 연결 성공 - 멱등성 서비스가 Redis를 활용하여 최적화된 성능으로 동작합니다.");
                log.info("📊 Redis 기능: 캐싱, 동시성 제어, 빠른 응답 재사용");
            } else {
                log.warn("⚠️ Redis 연결 불안정 - 예상치 못한 응답: {}. DB fallback으로 동작합니다.", result);
            }
        } catch (Exception e) {
            log.warn("⚠️ Redis 연결 실패 - DB fallback으로 동작합니다. Redis 설정을 확인하세요: spring.data.redis.host, spring.data.redis.port", e);
        }
    }

    @Override
    @Transactional
    public <T> T executeWithIdempotency(String endpoint, String key, Class<T> responseType, Supplier<T> operation) {
        validateKey(key);
        log.info("[Idemp] 멱등 처리 요청: endpoint={}, key={}", endpoint, key);

        // 1) Redis 해시 우선 조회 (COMPLETED 응답 재활용)
        String dataKey = buildDataKey(endpoint, key);
        Optional<String> cachedResponse = readCompletedResponseFromRedis(dataKey);
        if (cachedResponse.isPresent()) {
            IdempotencyReplayContextHolder.set(new IdempotencyReplayContext(key, true, "redis", null));
            log.info("[Idemp] Redis 캐시 적중(완료 응답 재사용): dataKey={}", dataKey);
            return deserialize(cachedResponse.get(), responseType);
        }

        // 2) DB 조회 (기존 JPA 저장소)
        Optional<IdempotencyKey> existing = findByIdempotencyKey(endpoint, key);
        if (existing.isPresent()) {
            Long recordedAt = existing.get().getCreatedAt() != null ? existing.get().getCreatedAt().toEpochMilli() : null;
            IdempotencyReplayContextHolder.set(new IdempotencyReplayContext(key, true, "db", recordedAt));
            log.info("[Idemp] DB 적중(완료 응답 재사용): endpoint={}, key={}", endpoint, key);
            return deserialize(existing.get().getResponseJson(), responseType);
        }

        // Redis를 사용할 수 있으면 예약(PENDING) 락을 시도
        String lockKey = buildLockKey(endpoint, key);
        boolean acquired = tryAcquireReservation(lockKey);
        if (!acquired) {
            log.warn("[Idemp] 예약 락 획득 실패(경합): lockKey={}", lockKey);
            // 이미 처리 중이거나 직후 완료된 경우: 재조회하여 완료 응답이 있으면 재사용, 없으면 충돌 반환
            Optional<String> againFromRedis = readCompletedResponseFromRedis(dataKey);
            if (againFromRedis.isPresent()) {
                log.info("[Idemp] 재시도 중 Redis 적중: dataKey={}", dataKey);
                return deserialize(againFromRedis.get(), responseType);
            }
            Optional<IdempotencyKey> after = findByIdempotencyKey(endpoint, key);
            if (after.isPresent()) {
                log.info("[Idemp] 재시도 중 DB 적중: endpoint={}, key={}", endpoint, key);
                return deserialize(after.get().getResponseJson(), responseType);
            }
            throw new IdempotencyHandler(IdempotencyErrorStatus.CONCURRENT_CONFLICT);
        }

        try {
            log.info("[Idemp] 예약 락 획득 성공: lockKey={}", lockKey);
            T result = operation.get();
            String responseJson = serialize(result);
            saveIdempotencyKey(endpoint, key, responseJson);
            IdempotencyReplayContextHolder.set(new IdempotencyReplayContext(key, false, "fresh", Instant.now().toEpochMilli()));
            
            // 트랜잭션 커밋 후에만 Redis에 기록하여 정합성 보장
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        log.debug("[Idemp] 트랜잭션 커밋 후 Redis 기록: dataKey={}", dataKey);
                        writeCompletedToRedis(dataKey, responseJson);
                    }
                });
            } else {
                // 방어적 처리: 트랜잭션 동기화가 없으면 즉시 기록
                log.debug("[Idemp] 즉시 Redis 기록(동기화 없음): dataKey={}", dataKey);
                writeCompletedToRedis(dataKey, responseJson);
            }
            
            log.info("[Idemp] 멱등 처리 완료: endpoint={}, key={}", endpoint, key);
            return result;
        } finally {
            // 락 해제: TTL이 있지만 완료 시 즉시 해제 시도
            log.debug("[Idemp] 예약 락 해제 시도: lockKey={}", lockKey);
            releaseReservation(lockKey);
        }
    }

    @Override
    @Cacheable(cacheNames = CACHE_NAME, key = "#endpoint + ':' + #key")
    public Optional<IdempotencyKey> findByIdempotencyKey(String endpoint, String key) {
        validateKey(key);
        try {
            log.debug("[Idemp] DB 조회: endpoint={}, key={}", endpoint, key);
            return repository.findByEndpointAndKey(endpoint, key);
        } catch (RuntimeException e) {
            log.warn("[Idemp] DB 조회 실패: endpoint={}, key={}", endpoint, key);
            throw new IdempotencyHandler(IdempotencyErrorStatus.REPOSITORY_ERROR);
        }
    }

    @Override
    @Transactional
    @CachePut(cacheNames = CACHE_NAME, key = "#endpoint + ':' + #key")
    public IdempotencyKey saveIdempotencyKey(String endpoint, String key, String responseJson) {
        if (responseJson == null) {
            throw new IdempotencyHandler(IdempotencyErrorStatus.REPOSITORY_ERROR);
        }
        try {
            log.debug("[Idemp] DB 저장 시도: endpoint={}, key={}", endpoint, key);
            IdempotencyKey entity = new IdempotencyKey(endpoint, key, responseJson, Instant.now());
            IdempotencyKey saved = repository.save(entity);
            log.info("[Idemp] DB 저장 성공: id={}, endpoint={}, key={}", saved.getId(), endpoint, key);
            return saved;
        } catch (DataIntegrityViolationException e) {
            // 중복 키 제약 조건 위반 시 기존 엔티티 조회하여 반환
            log.warn("[Idemp] DB 저장 충돌(중복 키): endpoint={}, key={}", endpoint, key);
            Optional<IdempotencyKey> existing = repository.findByEndpointAndKey(endpoint, key);
            if (existing.isPresent()) {
                log.info("[Idemp] 기존 엔티티 반환: id={}", existing.get().getId());
                return existing.get();
            }
            // 기존 엔티티가 없으면 예외를 재발생 (예상치 못한 상황)
            log.error("[Idemp] 중복 충돌 후 기존 엔티티 없음 - 불일치 상태");
            throw new IdempotencyHandler(IdempotencyErrorStatus.REPOSITORY_ERROR);
        } catch (RuntimeException e) {
            log.warn("[Idemp] DB 저장 실패: endpoint={}, key={}", endpoint, key);
            throw new IdempotencyHandler(IdempotencyErrorStatus.REPOSITORY_ERROR);
        }
    }

    private void validateKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IdempotencyHandler(IdempotencyErrorStatus.INVALID_IDEMPOTENCY_KEY);
        }
    }

    private String buildDataKey(String endpoint, String key) {
        return REDIS_NAMESPACE + encode(endpoint) + ':' + encode(key);
    }

    private String buildLockKey(String endpoint, String key) {
        return REDIS_LOCK_NAMESPACE + encode(endpoint) + ':' + encode(key);
    }

    private String encode(String raw) {
        // 간단한 키 인코딩: 공백 및 콜론 등 구분자 치환
        return raw.replace(" ", "_").replace(":", "|");
    }

    private boolean tryAcquireReservation(String lockKey) {
        // 짧은 재시도 정책(스핀): 경합 시 짧게 재시도 후 포기
        int attempts = 3;
        while (attempts-- > 0) {
            Boolean ok = redisTemplate.opsForValue().setIfAbsent(lockKey, STATUS_PENDING, RESERVATION_TTL);
            if (Boolean.TRUE.equals(ok)) return true;
            // 30ms 대기 (스레드 인터럽트 안전)
            LockSupport.parkNanos(30L * 1_000_000L);
            if (Thread.currentThread().isInterrupted()) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return false;
    }

    private void releaseReservation(String lockKey) {
        try {
            redisTemplate.delete(lockKey);
        } catch (RuntimeException ignored) {
            // TTL에 의해 자연 해제됨
        }
    }

    private Optional<String> readCompletedResponseFromRedis(String dataKey) {
        try {
            String status = (String) redisTemplate.opsForHash().get(dataKey, FIELD_STATUS);
            if (STATUS_COMPLETED.equals(status)) {
                String response = (String) redisTemplate.opsForHash().get(dataKey, FIELD_RESPONSE);
                if (response != null) return Optional.of(response);
            }
            return Optional.empty();
        } catch (RuntimeException e) {
            log.warn("[Idemp] Redis 조회 실패: dataKey={}", dataKey);
            return Optional.empty();
        }
    }

    private void writeCompletedToRedis(String dataKey, String responseJson) {
        try {
            Instant now = Instant.now();
            Instant expiresAt = now.plus(RESERVATION_TTL);
            Map<String, String> map = Map.of(
                    FIELD_STATUS, STATUS_COMPLETED,
                    FIELD_RESPONSE, responseJson,
                    FIELD_CREATED_AT, String.valueOf(now.toEpochMilli()),
                    FIELD_EXPIRES_AT, String.valueOf(expiresAt.toEpochMilli())
            );
            redisTemplate.opsForHash().putAll(dataKey, map);
            redisTemplate.expire(dataKey, RESERVATION_TTL);
            log.debug("[Idemp] Redis 기록 성공: dataKey={}, ttl={}s", dataKey, RESERVATION_TTL.toSeconds());
        } catch (RuntimeException ignored) {
            // 캐시 저장 실패는 기능 저하로 묵살 (DB에는 저장됨)
        }
    }

    private <T> String serialize(T result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            throw new IdempotencyHandler(IdempotencyErrorStatus.RESPONSE_SERIALIZATION_ERROR);
        }
    }

    private <T> T deserialize(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new IdempotencyHandler(IdempotencyErrorStatus.REPOSITORY_ERROR);
        }
    }
}


