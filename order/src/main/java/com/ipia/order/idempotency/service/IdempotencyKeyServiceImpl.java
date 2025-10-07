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
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    @Cacheable(cacheNames = CACHE_NAME, key = "#endpoint + ':' + #key")
    public Optional<IdempotencyKey> findByIdempotencyKey(String endpoint, String key) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    @Transactional
    @CachePut(cacheNames = CACHE_NAME, key = "#endpoint + ':' + #key")
    public IdempotencyKey saveIdempotencyKey(String endpoint, String key, String responseJson) {
        throw new UnsupportedOperationException("not implemented");
    }

    private void validateKey(String key) {
        throw new UnsupportedOperationException("not implemented");
    }

    private <T> String serialize(T result) {
        throw new UnsupportedOperationException("not implemented");
    }

    private <T> T deserialize(String json) {
        throw new UnsupportedOperationException("not implemented");
    }
}


