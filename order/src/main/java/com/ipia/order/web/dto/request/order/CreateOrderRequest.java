package com.ipia.order.web.dto.request.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주문 생성 요청 DTO
 * memberId는 JWT 토큰에서 추출하므로 요청 본문에서 제외
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    /**
     * 주문 총액
     */
    @NotNull(message = "주문 총액은 필수입니다")
    @Min(value = 1, message = "주문 총액은 1원 이상이어야 합니다")
    private Long totalAmount;
}
