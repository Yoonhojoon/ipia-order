package com.ipia.order.idempotency.repository;

import com.ipia.order.idempotency.domain.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 멱등성 키 저장소 계약(영속 기술은 이후 단계에서 연결).
 */
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, Long> {

    Optional<IdempotencyKey> findByEndpointAndKey(String endpoint, String key);


}


