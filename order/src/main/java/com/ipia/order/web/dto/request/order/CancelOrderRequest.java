package com.ipia.order.web.dto.request.order;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주문 취소 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderRequest {

    /**
     * 취소 사유
     */
    @NotBlank(message = "취소 사유는 필수입니다")
    private String reason;
}
