package com.ipia.order.idempotency.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ipia.order.idempotency.domain.IdempotencyKey;

/**
 * 멱등성 키 저장소 계약.
 */
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, Long> {

    Optional<IdempotencyKey> findByEndpointAndKey(String endpoint, String key);


}


