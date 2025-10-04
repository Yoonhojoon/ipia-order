package com.ipia.order.web.controller;

import com.ipia.order.common.exception.ApiResponse;
import com.ipia.order.member.service.MemberService;
import com.ipia.order.web.dto.request.MemberPasswordRequest;
import com.ipia.order.web.dto.request.MemberSignupRequest;
import com.ipia.order.web.dto.request.MemberUpdateRequest;
import com.ipia.order.web.dto.response.MemberResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 회원 관리 컨트롤러
 */
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 회원가입
     * POST /api/members
     */
    @PostMapping
    public ResponseEntity<ApiResponse<MemberResponse>> signup(@Valid @RequestBody MemberSignupRequest request) {
        // TODO: 구현 예정
        throw new UnsupportedOperationException("구현 예정");
    }

    /**
     * 회원 조회 (ID)
     * GET /api/members/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberResponse>> findById(@PathVariable Long id) {
        // TODO: 구현 예정
        throw new UnsupportedOperationException("구현 예정");
    }

    /**
     * 회원 조회 (이메일)
     * GET /api/members/email/{email}
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<MemberResponse>> findByEmail(@PathVariable String email) {
        // TODO: 구현 예정
        throw new UnsupportedOperationException("구현 예정");
    }

    /**
     * 전체 회원 조회
     * GET /api/members
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<MemberResponse>>> findAll() {
        // TODO: 구현 예정
        throw new UnsupportedOperationException("구현 예정");
    }

    /**
     * 회원 조회 (이름)
     * GET /api/members/search?name={name}
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<MemberResponse>>> findByName(@RequestParam String name) {
        // TODO: 구현 예정
        throw new UnsupportedOperationException("구현 예정");
    }

    /**
     * 회원 정보 수정
     * PUT /api/members/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberResponse>> update(@PathVariable Long id, 
                                               @Valid @RequestBody MemberUpdateRequest request) {
        // TODO: 구현 예정
        throw new UnsupportedOperationException("구현 예정");
    }

    /**
     * 비밀번호 변경
     * PUT /api/members/{id}/password
     */
    @PutMapping("/{id}/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(@PathVariable Long id, 
                                              @Valid @RequestBody MemberPasswordRequest request) {
        // TODO: 구현 예정
        throw new UnsupportedOperationException("구현 예정");
    }

    /**
     * 회원 탈퇴
     * DELETE /api/members/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> withdraw(@PathVariable Long id) {
        // TODO: 구현 예정
        throw new UnsupportedOperationException("구현 예정");
    }
}
