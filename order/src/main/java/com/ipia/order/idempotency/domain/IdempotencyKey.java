package com.ipia.order.idempotency.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

/**
 * 멱등성 키 엔티티.
 */
@Entity
@Table(name = "idempotency_keys",
        uniqueConstraints = @UniqueConstraint(name = "uk_idemp_endpoint_key", columnNames = {"endpoint", "idempotency_key"}))
public class IdempotencyKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String endpoint;

    @Column(name = "idempotency_key", nullable = false, length = 255)
    private String key;

    @Lob
    @Column(name = "response_json", nullable = false)
    private String responseJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected IdempotencyKey() {
        // for JPA
    }

    public IdempotencyKey(String endpoint, String key, String responseJson, Instant createdAt) {
        this.endpoint = endpoint;
        this.key = key;
        this.responseJson = responseJson;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
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


