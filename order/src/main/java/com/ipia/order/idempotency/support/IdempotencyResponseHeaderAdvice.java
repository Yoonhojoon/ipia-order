package com.ipia.order.idempotency.support;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
public class IdempotencyResponseHeaderAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(@NonNull MethodParameter returnType, @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return true; // 전역 적용
    }

    @Override
    public Object beforeBodyWrite(@Nullable Object body, @NonNull MethodParameter returnType, @NonNull MediaType selectedContentType,
                                  @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  @NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response) {
        IdempotencyReplayContext ctx = IdempotencyReplayContextHolder.get();
        try {
            if (ctx != null) {
                response.getHeaders().add("X-Idempotency-Key", ctx.getIdempotencyKey());
                response.getHeaders().add("X-Idempotent-Replayed", String.valueOf(ctx.isReplayed()));
                response.getHeaders().add("X-Idempotency-Source", ctx.getSource());
                if (ctx.getRecordedAtEpochMs() != null) {
                    response.getHeaders().add("X-Idempotency-Recorded-At", String.valueOf(ctx.getRecordedAtEpochMs()));
                }
            }
        } finally {
            IdempotencyReplayContextHolder.clear();
        }
        return body;
    }
}


