package com.ipia.order.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipia.order.idempotency.repository.IdempotencyKeyRepository;
import com.ipia.order.idempotency.service.IdempotencyKeyService;
import com.ipia.order.idempotency.service.IdempotencyKeyServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.GenericContainer;

import java.util.concurrent.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@DisplayName("IdempotencyKeyService - Redis 통합")
class IdempotencyKeyServiceRedisIT {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", () -> redis.getHost());
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    IdempotencyKeyRepository repository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    IdempotencyKeyServiceImpl sut;

    private static final String ENDPOINT = "POST /api/orders";

    @Test
    @DisplayName("동시 요청 10개 중 단 1개만 실제 실행되고 동일 결과 반환")
    void concurrentRequests_onlyOneExecutes_restReuseResult() throws Exception {
        int threads = 10;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        List<Callable<Map<String,Object>>> tasks = new ArrayList<>();
        String key = UUID.randomUUID().toString();

        for (int i = 0; i < threads; i++) {
            tasks.add(() -> {
                @SuppressWarnings({"unchecked", "rawtypes"})
                Map<String, Object> result = (Map<String, Object>) ((IdempotencyKeyService) sut).executeWithIdempotency(ENDPOINT, key, Object.class, () -> Map.of("ok", 1));
                return result;
            });
        }

        List<Future<Map<String,Object>>> futures = pool.invokeAll(tasks);
        pool.shutdown();

        int success = 0;
        for (Future<Map<String, Object>> f : futures) {
            Map<String, Object> res = f.get(5, TimeUnit.SECONDS);
            assertThat(res.get("ok")).isEqualTo(1);
            success++;
        }
        assertThat(success).isEqualTo(threads);
    }
}


