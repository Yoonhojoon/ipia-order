package com.ipia.order.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * 테스트 설정 클래스
 */
@TestConfiguration
public class TestConfig {

    /**
     * 테스트용 ObjectMapper 설정
     * LocalDateTime 직렬화/역직렬화 지원
     */
    @Bean
    @Primary
    public ObjectMapper testObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }
}
