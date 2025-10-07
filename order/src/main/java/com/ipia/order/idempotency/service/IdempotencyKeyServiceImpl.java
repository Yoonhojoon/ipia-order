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

import java.time.Instant;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IdempotencyKeyServiceImpl implements IdempotencyKeyService {

    private static final String CACHE_NAME = "idemp";

    private final IdempotencyKeyRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public <T> T executeWithIdempotency(String endpoint, String key, Supplier<T> operation) {
        validateKey(key);

        Optional<IdempotencyKey> existing = findByIdempotencyKey(endpoint, key);
        if (existing.isPresent()) {
            return deserialize(existing.get().getResponseJson());
        }

        T result = operation.get();
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
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            throw new IdempotencyHandler(IdempotencyErrorStatus.RESPONSE_SERIALIZATION_ERROR);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T deserialize(String json) {
        try {
            // 일반화된 역직렬화: Object로 읽고 그대로 반환
            return (T) objectMapper.readValue(json, Object.class);
        } catch (JsonProcessingException e) {
            throw new IdempotencyHandler(IdempotencyErrorStatus.REPOSITORY_ERROR);
        }
    }
}


