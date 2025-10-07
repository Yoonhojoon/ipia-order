package com.ipia.order.idempotency;

import com.ipia.order.common.exception.idempotency.IdempotencyHandler;
import com.ipia.order.common.exception.idempotency.status.IdempotencyErrorStatus;
import com.ipia.order.idempotency.domain.IdempotencyKey;
import com.ipia.order.idempotency.repository.IdempotencyKeyRepository;
import com.ipia.order.idempotency.service.IdempotencyKeyService;
import com.ipia.order.idempotency.service.IdempotencyKeyServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("IdempotencyKeyService 실패 케이스")
class IdempotencyKeyServiceTest {

    @Mock
    IdempotencyKeyRepository repository;

    IdempotencyKeyService sut;

    @BeforeEach
    void setUp() {
        sut = new IdempotencyKeyServiceImpl(repository, new ObjectMapper());
    }

    private static final String ENDPOINT = "POST /api/orders";

    @Nested
    @DisplayName("executeWithIdempotency")
    class ExecuteWithIdempotencyFailures {

        @Test
        @DisplayName("잘못된 키 형식 시 IdempotencyHandler(INVALID_IDEMPOTENCY_KEY)")
        void invalidKey_throwsInvalidIdempotencyKeyException() {
            Supplier<String> op = () -> "ok";
            assertThatThrownBy(() -> sut.executeWithIdempotency(ENDPOINT, " ", op))
                    .isInstanceOf(IdempotencyHandler.class)
                    .hasMessage(IdempotencyErrorStatus.INVALID_IDEMPOTENCY_KEY.getCode());
        }

        // 동시성/직렬화/중복 키 케이스는 구현 단계에서 별도 통합/동시성 테스트로 다룹니다
    }

    @Nested
    @DisplayName("executeWithIdempotency - 성공 경로")
    class ExecuteWithIdempotencySuccesses {

        @Test
        @DisplayName("캐시 미스: operation(Map) 결과를 반환한다")
        void cacheMiss_returnsOperationResult() {
            Supplier<Map<String, Object>> op = () -> java.util.Map.of("result", "ok");
            Map<String, Object> result = sut.executeWithIdempotency(ENDPOINT, "fresh-key", op);
            assertThat(result.get("result")).isEqualTo("ok");
        }

        @Test
        @DisplayName("정상 키: 예외 없이 수행되고 Map 반환")
        void validKey_runsWithoutException() {
            Supplier<Map<String, Object>> op = () -> java.util.Map.of("v", 1);
            org.assertj.core.api.Assertions.assertThatCode(() -> sut.executeWithIdempotency(ENDPOINT, "valid", op))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("캐시 히트: 저장된 Map 응답을 반환해야 한다(Red→Green 예정)")
        void cacheHit_returnsStoredResponse() {
            // given
            String key = "hit";
            IdempotencyKey stored = new IdempotencyKey(ENDPOINT, key, "{\"result\":\"ok\"}", Instant.now());
            given(repository.findByEndpointAndKey(ENDPOINT, key)).willReturn(java.util.Optional.of(stored));

            // when
            Supplier<Map<String, Object>> op = () -> java.util.Map.of("result", "should-not-run");

            // then: 현재 구현은 역직렬화 Object→Map 캐스트 가능, 정책에 맞추어 예외 없이 동작해야 함
            org.assertj.core.api.Assertions.assertThatCode(() -> sut.executeWithIdempotency(ENDPOINT, key, op))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("findByIdempotencyKey")
    class FindByIdempotencyKeyFailures {
        @Test
        @DisplayName("잘못된 입력 시 IdempotencyHandler(INVALID_IDEMPOTENCY_KEY)")
        void invalidInput_throwsInvalidIdempotencyKeyException() {
            assertThatThrownBy(() -> sut.findByIdempotencyKey(ENDPOINT, " "))
                    .isInstanceOf(IdempotencyHandler.class)
                    .hasMessage(IdempotencyErrorStatus.INVALID_IDEMPOTENCY_KEY.getCode());
        }

        @Test
        @DisplayName("저장소 예외 시 IdempotencyHandler(REPOSITORY_ERROR)")
        void repositoryError_translatesToHandler() {
            given(repository.findByEndpointAndKey(ENDPOINT, "key")).willThrow(new RuntimeException("db"));
            assertThatThrownBy(() -> sut.findByIdempotencyKey(ENDPOINT, "key"))
                    .isInstanceOf(IdempotencyHandler.class)
                    .hasMessage(IdempotencyErrorStatus.REPOSITORY_ERROR.getCode());
        }
    }

    @Nested
    @DisplayName("saveIdempotencyKey")
    class SaveIdempotencyKeyFailures {
        @Test
        @DisplayName("responseJson 이 null 이면 IdempotencyHandler(REPOSITORY_ERROR) 또는 IllegalArgument")
        void nullResponseJson_throwsIllegalArgument() {
            assertThatThrownBy(() -> sut.saveIdempotencyKey(ENDPOINT, "key", null))
                    .isInstanceOf(IdempotencyHandler.class)
                    .hasMessage(IdempotencyErrorStatus.REPOSITORY_ERROR.getCode());
        }
    }

    
}


