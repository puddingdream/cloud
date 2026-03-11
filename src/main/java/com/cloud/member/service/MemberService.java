package com.cloud.member.service;

import com.cloud.member.dto.MemberCreateRequest;
import com.cloud.member.dto.MemberCreateResponse;
import com.cloud.member.dto.MemberGetOneResponse;
import com.cloud.member.entity.Member;
import com.cloud.member.repository.MemberRepositroy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepositroy memberRepositroy;

    @Transactional
    public MemberCreateResponse save(MemberCreateRequest request) {
        return new MemberCreateResponse(memberRepositroy.save(Member.from(request)));
    }

    @Transactional(readOnly = true)
    public MemberGetOneResponse getOne(Long memberId) {
        Member member = memberRepositroy.findById(memberId).orElseThrow(
                () -> new IllegalArgumentException("없는 멤버입니다.")
        );
        return new MemberGetOneResponse(member);
    }
}
