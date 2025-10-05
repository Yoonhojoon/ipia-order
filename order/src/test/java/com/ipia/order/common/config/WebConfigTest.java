package com.ipia.order.common.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * WebConfig 테스트
 */
@SpringBootTest
class WebConfigTest {

    @Autowired
    private WebConfig webConfig;

    @Test
    @DisplayName("WebConfig가 올바르게 로드되는지 확인")
    void webConfigLoaded() {
        // given & when & then
        assertThat(webConfig).isNotNull();
        assertThat(webConfig).isInstanceOf(WebMvcConfigurer.class);
    }
}
