package com.cloud.member.service;

import com.cloud.common.exception.ErrorCode;
import com.cloud.common.exception.MemberException;
import com.cloud.member.dto.MemberCreateRequest;
import com.cloud.member.dto.MemberCreateResponse;
import com.cloud.member.dto.MemberGetOneResponse;
import com.cloud.member.dto.MemberProfileImageResponse;
import com.cloud.member.entity.Member;
import com.cloud.member.repository.MemberRepositroy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepositroy memberRepositroy;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    @Transactional
    public MemberCreateResponse save(MemberCreateRequest request) {
        return new MemberCreateResponse(memberRepositroy.save(Member.from(request)));
    }

    @Transactional(readOnly = true)
    public MemberGetOneResponse getOne(Long memberId) {
        Member member = findMember(memberId);
        return new MemberGetOneResponse(member);
    }

    @Transactional
    public void uploadProfileImage(Long memberId, MultipartFile profileImage) throws IOException {
        // 1) 업로드 요청의 파일 유효성 확인
        //    - profileImage == null : 아예 파일 파트가 안 넘어온 경우
        //    - profileImage.isEmpty() : 파일명은 있지만 실제 내용이 비어 있는 경우
        //    둘 다 정상 업로드가 아니므로 400(BAD_REQUEST) 성격의 예외를 던진다.
        if (profileImage == null || profileImage.isEmpty()) {
            throw new MemberException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 2) "어느 회원의 프로필 이미지인지"를 확인하기 위해 회원을 먼저 조회한다.
        //    회원이 없으면 업로드 대상이 없으므로 404 예외가 발생한다.
        Member member = findMember(memberId);

        // 3) S3 key(버킷 내부 경로)를 만든다.
        //    같은 파일명을 여러 번 업로드해도 덮어쓰기 되지 않게 UUID를 앞에 붙인다.
        //    예) members/1/profile/UUID-avatar.png
        String key = createProfileImageKey(memberId, profileImage.getOriginalFilename());

        // 4) S3 PutObject 요청 메타데이터를 구성한다.
        //    - bucket: 어느 버킷에 저장할지
        //    - key: 버킷 안에서 어떤 이름(경로)으로 저장할지
        //    - contentType: 이미지 타입 정보(image/png, image/jpeg 등)
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(profileImage.getContentType())
                .build();

        // 5) 실제 업로드 실행
        //    MultipartFile의 바이트를 한 번에 통째로 들고 있지 않고,
        //    InputStream 형태로 S3에 흘려보내는(스트리밍) 방식으로 전송한다.
        //    두 번째 파라미터의 profileImage.getSize()는 전체 바이트 길이다.
        s3Client.putObject(
                request,
                RequestBody.fromInputStream(profileImage.getInputStream(), profileImage.getSize())
        );

        // 6) 업로드 성공 후 DB의 회원 정보에 S3 key를 저장한다.
        //    여기서 URL을 저장하지 않는 이유:
        //    Presigned URL은 만료 시간이 있는 임시 주소이기 때문이다.
        //    따라서 DB에는 변하지 않는 key를 저장하고, 조회 API에서 URL을 매번 새로 발급한다.
        member.updateProfileImageKey(key);
    }

    @Transactional(readOnly = true)
    public MemberProfileImageResponse getProfileImage(Long memberId) {
        // 1) 요청으로 들어온 memberId가 실제 존재하는 회원인지 먼저 확인한다.
        //    없으면 findMember 내부에서 MEMBER_NOT_FOUND 예외가 발생한다.
        Member member = findMember(memberId);

        // 2) DB에서 해당 회원의 "프로필 이미지 key"를 꺼낸다.
        //    주의: DB에는 전체 URL이 아니라 key만 저장되어 있다.
        //    예) members/1/profile/uuid-avatar.png
        String profileImageKey = member.getProfileImageKey();

        // 3) 이미지 key가 없으면 아직 업로드를 안 한 상태이므로 404 에러를 내려준다.
        //    (빈 문자열/공백도 업로드 안 된 상태로 본다)
        if (profileImageKey == null || profileImageKey.isBlank()) {
            throw new MemberException(ErrorCode.PROFILE_IMAGE_NOT_FOUND);
        }

        // 4) 이제 어떤 S3 파일을 다운로드할지 지정한다.
        //    bucket + key 조합으로 "대상 파일 하나"가 결정된다.
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(profileImageKey)
                .build();

        // 5) Presigned URL을 발급한다.
        //    signatureDuration(Duration.ofDays(7))을 써서 URL 만료 시간을 7일로 고정한다.
        //    즉, 지금 발급한 URL은 7일 뒤 자동으로 무효가 된다.
        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofDays(7))
                        .getObjectRequest(getObjectRequest)
                        .build()
        );

        // 6) 클라이언트가 쓰기 편하도록 응답 DTO로 감싸서 반환한다.
        //    - url: 브라우저/앱에서 바로 호출 가능한 다운로드 주소
        //    - expiresAt: 이 URL이 만료되는 정확한 시각
        return new MemberProfileImageResponse(
                presignedRequest.url().toString(),
                presignedRequest.expiration()
        );
    }

    private Member findMember(Long memberId) {
        return memberRepositroy.findById(memberId).orElseThrow(
                () -> new MemberException(ErrorCode.MEMBER_NOT_FOUND)
        );
    }

    private String createProfileImageKey(Long memberId, String originalFileName) {
        // 원본 파일명이 null/blank일 수 있으므로 기본 파일명으로 보정한다.
        String safeName = (originalFileName == null || originalFileName.isBlank()) ? "profile-image" : originalFileName;

        // key 형식:
        // members/{memberId}/profile/{uuid}-{원본파일명}
        // 이렇게 폴더처럼 나누면 S3에서 관리하기 쉬워진다.
        return "members/%d/profile/%s-%s".formatted(memberId, UUID.randomUUID(), safeName);
    }
}
