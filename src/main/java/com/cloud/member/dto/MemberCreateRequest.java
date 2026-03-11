package com.cloud.member.dto;

import com.cloud.member.enums.MbtiType;

public record MemberCreateRequest(
        String name,
        int age,
        MbtiType mbtiType
) {
}
