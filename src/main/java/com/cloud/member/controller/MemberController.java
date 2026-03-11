package com.cloud.member.controller;

import com.cloud.common.dto.ApiResponse;
import com.cloud.member.dto.MemberCreateRequest;
import com.cloud.member.dto.MemberCreateResponse;
import com.cloud.member.dto.MemberGetOneResponse;
import com.cloud.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/api/members")
    public ResponseEntity<ApiResponse<MemberCreateResponse>> createMember(
            @RequestBody MemberCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(memberService.save(request)));
    }

    @GetMapping("/api/members/{id}")
    public ResponseEntity<ApiResponse<MemberGetOneResponse>> getOne(
            @PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(memberService.getOne(id)));
    }
}

