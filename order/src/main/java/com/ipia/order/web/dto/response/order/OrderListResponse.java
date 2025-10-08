package com.ipia.order.web.dto.response.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 주문 목록 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderListResponse {

    /**
     * 주문 목록
     */
    private List<OrderResponse> orders;

    /**
     * 전체 주문 수
     */
    private long totalCount;

    /**
     * 현재 페이지 번호
     */
    private int page;

    /**
     * 페이지 크기
     */
    private int size;

    /**
     * 전체 페이지 수
     */
    private int totalPages;
}
