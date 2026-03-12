package com.cloud.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

    /**
     * 애플리케이션에서 발생할 수 있는 에러들을 정의한 Enum 클래스입니다.
     * 에러 코드, 상태 코드, 메시지를 한 곳에서 관리하여 일관된 에러 응답을 보장합니다.
     */
    @Getter
    @RequiredArgsConstructor
    public enum ErrorCode {

    // [C] COMMON - 공통
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 내부 오류가 발생했습니다."),

    // [M] DOMAIN(공통/회원) - 요청/검증 관련 (400)


    // [M] NOT_FOUND - 리소스를 찾을 수 없음 (404)
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M004", "회원을 찾을 수 없습니다."),
    PROFILE_IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "M005", "프로필 이미지를 찾을 수 없습니다."),



    // FORBIDDEN(403) - 접근/상태 제한
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "P010", "접근 권한이 없습니다."),
    INVALID_PROFILE(HttpStatus.BAD_REQUEST, "P007", "사용자가 일치하지 않습니다."),


    // [M] CONFLICT - 상태 충돌/규칙 위반 (409)
    INVALID_STATUS_CHANGE(HttpStatus.CONFLICT, "M010", "허용되지 않은 상태 전환입니다.");

    private final HttpStatus status; // HTTP 상태 코드 (예: 400, 404, 500)
    private final String code;       // 우리가 정의한 고유 에러 코드 (예: M001)
    private final String message;    // 사용자에게 보여줄 에러 메시지
}
