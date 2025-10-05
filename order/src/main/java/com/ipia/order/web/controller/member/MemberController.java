package com.ipia.order.web.controller.member;

import com.ipia.order.common.exception.ApiErrorCodeExample;
import com.ipia.order.common.exception.ApiErrorCodeExamples;
import com.ipia.order.common.exception.ApiResponse;
import com.ipia.order.common.exception.member.status.MemberErrorStatus;
import com.ipia.order.common.exception.member.status.MemberSuccessStatus;
import com.ipia.order.member.domain.Member;
import com.ipia.order.member.service.MemberService;
import com.ipia.order.web.dto.request.MemberPasswordRequest;
import com.ipia.order.web.dto.request.MemberUpdateRequest;
import com.ipia.order.web.dto.response.MemberResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "회원 관리", description = "회원 조회, 수정, 탈퇴 등의 회원 관리 API")
public class MemberController {

    private final MemberService memberService;

    // 회원가입 엔드포인트는 AuthController로 이전되었습니다.

    /**
     * 회원 조회 (ID)
     * GET /api/members/{id}
     */
    @Operation(summary = "ID로 회원 조회", description = "회원 ID를 통해 특정 회원 정보를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원 조회 성공", 
                    content = @Content(schema = @Schema(implementation = MemberResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER_NOT_FOUND", "INVALID_INPUT"})
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberResponse>> findById(
            @Parameter(description = "회원 ID", example = "1") @PathVariable("id") Long id) {
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
    @Operation(summary = "이메일로 회원 조회", description = "이메일 주소를 통해 특정 회원 정보를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원 조회 성공", 
                    content = @Content(schema = @Schema(implementation = MemberResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER_NOT_FOUND"})
    })
    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<MemberResponse>> findByEmail(
            @Parameter(description = "회원 이메일", example = "user@example.com") @PathVariable("email") String email) {
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
    @Operation(summary = "전체 회원 조회", description = "활성화된 모든 회원 목록을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원 목록 조회 성공", 
                    content = @Content(schema = @Schema(implementation = MemberResponse.class)))
    })
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
    @Operation(summary = "이름으로 회원 검색", description = "이름을 통해 회원을 검색합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원 검색 성공", 
                    content = @Content(schema = @Schema(implementation = MemberResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER_NOT_FOUND"})
    })
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<MemberResponse>>> findByName(
            @Parameter(description = "검색할 회원 이름", example = "홍길동") @RequestParam("name") String name) {
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
    @Operation(summary = "회원 정보 수정", description = "회원의 닉네임(이름)을 수정합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원 정보 수정 성공", 
                    content = @Content(schema = @Schema(implementation = MemberResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER_NOT_FOUND", "INVALID_INPUT", "INVALID_NICKNAME"})
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberResponse>> update(
            @Parameter(description = "회원 ID", example = "1") @PathVariable("id") Long id,
            @Valid @RequestBody MemberUpdateRequest request) {
        Member member = memberService.updateNickname(id, request.getName());
        MemberResponse response = MemberResponse.from(member);
        return ApiResponse.onSuccess(MemberSuccessStatus.MEMBER_UPDATED, response);
    }

    /**
     * 비밀번호 변경
     * PUT /api/members/{id}/password
     */
    @Operation(summary = "비밀번호 변경", description = "회원의 비밀번호를 변경합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER_NOT_FOUND", "INVALID_INPUT", "PASSWORD_MISMATCH", "PASSWORD_POLICY_VIOLATION"})
    })
    @PutMapping("/{id}/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @Parameter(description = "회원 ID", example = "1") @PathVariable("id") Long id,
            @Valid @RequestBody MemberPasswordRequest request) {
        memberService.updatePassword(id, request.getCurrentPassword(), request.getNewPassword());
        return ApiResponse.onSuccess(MemberSuccessStatus.PASSWORD_UPDATED);
    }

    /**
     * 회원 탈퇴
     * DELETE /api/members/{id}
     */
    @Operation(summary = "회원 탈퇴", description = "회원 계정을 비활성화합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원 탈퇴 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER_NOT_FOUND", "INVALID_INPUT", "ALREADY_INACTIVE"})
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @Parameter(description = "회원 ID", example = "1") @PathVariable("id") Long id) {
        memberService.withdraw(id);
        return ApiResponse.onSuccess(MemberSuccessStatus.MEMBER_WITHDRAWN);
    }
}
