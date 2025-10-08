package com.ipia.order.idempotency.service;

import com.ipia.order.idempotency.domain.IdempotencyKey;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * 멱등성 키를 이용해 비즈니스 연산의 결과를 캐싱/재사용하는 서비스 계약.
 */
public interface IdempotencyKeyService {

    /**
     * 주어진 엔드포인트와 멱등 키로 연산을 실행합니다. 기존 키가 존재하면 캐시된 응답을 재사용합니다.
     *
     * @param endpoint 엔드포인트 식별자(예: "POST /api/orders")
     * @param key 멱등성 키
     * @param responseType 응답 타입 클래스 (역직렬화용)
     * @param operation 실제 수행할 연산
     * @return 연산 결과
     */
    <T> T executeWithIdempotency(String endpoint, String key, Class<T> responseType, Supplier<T> operation);

    /**
     * 멱등성 키 엔트리를 조회합니다.
     */
    Optional<IdempotencyKey> findByIdempotencyKey(String endpoint, String key);

    /**
     * 멱등성 키 엔트리를 저장합니다.
     */
    IdempotencyKey saveIdempotencyKey(String endpoint, String key, String responseJson);
}


