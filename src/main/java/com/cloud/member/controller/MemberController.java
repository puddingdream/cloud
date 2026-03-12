package com.cloud.member.controller;

import com.cloud.common.dto.ApiResponse;
import com.cloud.member.dto.MemberCreateRequest;
import com.cloud.member.dto.MemberCreateResponse;
import com.cloud.member.dto.MemberGetOneResponse;
import com.cloud.member.dto.MemberProfileImageResponse;
import com.cloud.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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

    @PostMapping("/api/members/{id}/profile-image")
    public ResponseEntity<ApiResponse<Void>> uploadProfileImage(
            @PathVariable Long id,
            // multipart/form-data 요청의 "파일 파트"를 받는 자리다.
            // 클라이언트에서 form-data key를 profileImage로 보내면,
            // 스프링이 그 파일을 MultipartFile 객체로 만들어 이 변수에 넣어준다.
            //
            // 예) Postman form-data
            // - key: profileImage
            // - type: File
            // - value: cat.png
            @RequestParam("profileImage") MultipartFile profileImage) throws IOException {
        // 실제 업로드/저장 로직은 서비스 계층에서 처리한다.
        // 컨트롤러는 요청 파라미터를 받고 서비스 호출 + 응답 반환 역할만 담당한다.
        memberService.uploadProfileImage(id, profileImage);

        // 업로드 성공 시 data 없이 success=true 형태로 응답한다.
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(null));
    }

    @GetMapping("/api/members/{id}/profile-image")
    public ResponseEntity<ApiResponse<MemberProfileImageResponse>> getProfileImage(
            @PathVariable Long id) {
        // 이 API는 "이미지 파일 자체"를 바로 내려주는 API가 아니다.
        // 대신 S3 파일을 다운로드할 수 있는 "임시 주소(Presigned URL)"를 내려준다.
        // 이유: S3를 퍼블릭으로 열지 않고, 일정 시간 뒤 만료되는 안전한 주소를 쓰기 위해서다.
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(memberService.getProfileImage(id)));
    }
}

