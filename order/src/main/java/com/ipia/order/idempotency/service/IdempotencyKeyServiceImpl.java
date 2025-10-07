package com.ipia.order.idempotency.service;

import com.ipia.order.common.exception.idempotency.IdempotencyHandler;
import com.ipia.order.common.exception.idempotency.status.IdempotencyErrorStatus;
import com.ipia.order.idempotency.domain.IdempotencyKey;
import com.ipia.order.idempotency.repository.IdempotencyKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IdempotencyKeyServiceImpl implements IdempotencyKeyService {

    private static final String CACHE_NAME = "idemp";

    private final IdempotencyKeyRepository repository;

    @Override
    @Transactional
    public <T> T executeWithIdempotency(String endpoint, String key, Supplier<T> operation) {
        validateKey(key);

        Optional<IdempotencyKey> existing = findByIdempotencyKey(endpoint, key);
        if (existing.isPresent()) {
            return deserialize(existing.get().getResponseJson());
        }

        T result;
        try {
            result = operation.get();
        } catch (RuntimeException e) {
            throw e; // 비즈니스 예외 전파
        }

        String responseJson = serialize(result);
        saveIdempotencyKey(endpoint, key, responseJson);
        return result;
    }

    @Override
    @Cacheable(cacheNames = CACHE_NAME, key = "#endpoint + ':' + #key")
    public Optional<IdempotencyKey> findByIdempotencyKey(String endpoint, String key) {
        validateKey(key);
        try {
            return repository.findByEndpointAndKey(endpoint, key);
        } catch (RuntimeException e) {
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
            IdempotencyKey entity = new IdempotencyKey(endpoint, key, responseJson, Instant.now());
            return repository.save(entity);
        } catch (RuntimeException e) {
            throw new IdempotencyHandler(IdempotencyErrorStatus.REPOSITORY_ERROR);
        }
    }

    private void validateKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IdempotencyHandler(IdempotencyErrorStatus.INVALID_IDEMPOTENCY_KEY);
        }
    }

    private <T> String serialize(T result) {
        try {
            return String.valueOf(result); // TODO: ObjectMapper 주입 후 교체
        } catch (RuntimeException e) {
            throw new IdempotencyHandler(IdempotencyErrorStatus.RESPONSE_SERIALIZATION_ERROR);
        }
    }

    private <T> T deserialize(String json) {
        // 테스트 단계에서는 사용하지 않음. 실제 구현 시 ObjectMapper 사용
        throw new UnsupportedOperationException("deserialize not implemented");
    }
}


