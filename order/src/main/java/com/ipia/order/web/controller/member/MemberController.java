package com.ipia.order.web.controller.member;

import com.ipia.order.common.exception.ApiResponse;
import com.ipia.order.common.exception.member.status.MemberErrorStatus;
import com.ipia.order.common.exception.member.status.MemberSuccessStatus;
import com.ipia.order.member.domain.Member;
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
        Member member = memberService.signup(request.getName(), request.getEmail());
        MemberResponse response = MemberResponse.from(member);
        return ApiResponse.onSuccess(MemberSuccessStatus.SIGN_UP_SUCCESS, response);
    }

    /**
     * 회원 조회 (ID)
     * GET /api/members/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberResponse>> findById(@PathVariable("id") Long id) {
        Member member = memberService.findById(id).orElse(null);
        if (member == null) {
            return ApiResponse.onFailure(MemberErrorStatus.MEMBER_NOT_FOUND, (MemberResponse) null);
        }
        MemberResponse response = MemberResponse.from(member);
        return ApiResponse.onSuccess(MemberSuccessStatus.MEMBER_FOUND, response);
    }

    /**
     * 회원 조회 (이메일)
     * GET /api/members/email/{email}
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<MemberResponse>> findByEmail(@PathVariable("email") String email) {
        Member member = memberService.findByEmail(email).orElse(null);
        if (member == null) {
            return ApiResponse.onFailure(MemberErrorStatus.MEMBER_NOT_FOUND, (MemberResponse) null);
        }
        MemberResponse response = MemberResponse.from(member);
        return ApiResponse.onSuccess(MemberSuccessStatus.MEMBER_FOUND, response);
    }

    /**
     * 전체 회원 조회
     * GET /api/members
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<MemberResponse>>> findAll() {
        List<Member> members = memberService.findAll();
        List<MemberResponse> responses = members.stream()
                .map(MemberResponse::from)
                .toList();
        return ApiResponse.onSuccess(MemberSuccessStatus.MEMBERS_FOUND, responses);
    }

    /**
     * 회원 조회 (이름)
     * GET /api/members/search?name={name}
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<MemberResponse>>> findByName(@RequestParam("name") String name) {
        List<Member> members = memberService.findByName(name);
        List<MemberResponse> responses = members.stream()
                .map(MemberResponse::from)
                .toList();
        return ApiResponse.onSuccess(MemberSuccessStatus.MEMBERS_FOUND, responses);
    }

    /**
     * 회원 정보 수정
     * PUT /api/members/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberResponse>> update(@PathVariable("id") Long id,
                                               @Valid @RequestBody MemberUpdateRequest request) {
        Member member = memberService.updateNickname(id, request.getName());
        MemberResponse response = MemberResponse.from(member);
        return ApiResponse.onSuccess(MemberSuccessStatus.MEMBER_UPDATED, response);
    }

    /**
     * 비밀번호 변경
     * PUT /api/members/{id}/password
     */
    @PutMapping("/{id}/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(@PathVariable("id") Long id,
                                              @Valid @RequestBody MemberPasswordRequest request) {
        memberService.updatePassword(id, request.getCurrentPassword(), request.getNewPassword());
        return ApiResponse.onSuccess(MemberSuccessStatus.PASSWORD_UPDATED);
    }

    /**
     * 회원 탈퇴
     * DELETE /api/members/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> withdraw(@PathVariable("id") Long id) {
        memberService.withdraw(id);
        return ApiResponse.onSuccess(MemberSuccessStatus.MEMBER_WITHDRAWN);
    }
}
