package com.cloud.member.dto;

import com.cloud.member.entity.Member;
import com.cloud.member.enums.MbtiType;

public record MemberCreateResponse(
        Long id,
        String name,
        int age,
        MbtiType mbtiType
) {
    public  MemberCreateResponse(Member member) {
        this(member.getId(),
                member.getName(),
                member.getAge(),
                member.getMbtiType());
    }
}
