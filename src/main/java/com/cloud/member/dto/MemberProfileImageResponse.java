package com.cloud.member.dto;

import java.time.Instant;

public record MemberProfileImageResponse(
        // S3에서 파일을 다운로드할 수 있는 임시 주소(Presigned URL)
        String url,
        // 위 URL이 만료되는 시각 (이 시간이 지나면 접근 불가)
        Instant expiresAt
) {
}
