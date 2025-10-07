package com.ipia.order.idempotency.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * 멱등성 키 엔티티(영속 모델은 이후 단계에서 구체화).
 */
public class IdempotencyKey {

    private final String endpoint;
    private final String key;
    private final String responseJson;
    private final Instant createdAt;

    public IdempotencyKey(String endpoint, String key, String responseJson, Instant createdAt) {
        this.endpoint = endpoint;
        this.key = key;
        this.responseJson = responseJson;
        this.createdAt = createdAt;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getKey() {
        return key;
    }

    public String getResponseJson() {
        return responseJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IdempotencyKey that = (IdempotencyKey) o;
        return Objects.equals(endpoint, that.endpoint) && Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(endpoint, key);
    }
}


