package com.ipia.order.web.dto.request.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주문 생성 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    /**
     * 회원 ID
     */
    @NotNull(message = "회원 ID는 필수입니다")
    @Min(value = 1, message = "회원 ID는 1 이상이어야 합니다")
    private Long memberId;

    /**
     * 주문 총액
     */
    @NotNull(message = "주문 총액은 필수입니다")
    @Min(value = 1, message = "주문 총액은 1원 이상이어야 합니다")
    private Long totalAmount;
}
