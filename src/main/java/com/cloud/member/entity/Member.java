package com.cloud.member.entity;

import com.cloud.member.dto.MemberCreateRequest;
import com.cloud.member.enums.MbtiType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private int age;
    @Column(nullable = false)
    private MbtiType mbtiType;

    public Member(String name, int age, MbtiType mbtiType) {
        this.name = name;
        this.age = age;
        this.mbtiType = mbtiType;
    }

    public static Member from(MemberCreateRequest request) {
        return new Member(
                request.name(),
                request.age(),
                request.mbtiType()
        );
    }
}
