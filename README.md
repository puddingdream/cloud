# Cloud 과제 제출 README

## 1. 프로젝트 소개
- Spring Boot 기반 API 서버를 AWS 환경에 배포하고, S3/RDS/EC2/ALB/ASG/CloudFront를 연동했습니다.
- 배포 자동화를 위해 GitHub Actions 기반 CI/CD 파이프라인을 구성했습니다.

## 2. 기술 스택
- Java 17
- Spring Boot 4
- Spring Data JPA
- MySQL (RDS)
- AWS (EC2, IAM Role, S3, Parameter Store, ALB, ASG, CloudFront, Route53)
- Docker, GitHub Actions

## 3. 주요 기능
- 회원 생성 API
- 회원 프로필 이미지 업로드 API (S3 업로드)
- 회원 프로필 이미지 조회 API (Presigned URL 반환)
- Actuator health 체크

## 4. 배포 환경 요약
- API 도메인: `https://api.devnest.click`
- Health Check: `https://api.devnest.click/actuator/health`
- CloudFront 도메인: `https://d1y67ty5km7ay2.cloudfront.net`
- S3 버킷: `camp-cloud-dorayaki-files`

## 5. 과제 수행 내용

### LV3. IAM Role + Presigned URL
- EC2 IAM Role 기반으로 S3/SSM 권한 설정
- 프로필 이미지 업로드 후 Presigned URL 발급/조회 확인

증빙:
- Presigned URL 응답 캡처: `<사진>`
- URL 접근 성공 캡처: `<사진>`

### LV4. Docker + CI
- 애플리케이션 Docker 이미지 빌드
- GitHub Actions로 빌드/푸시 자동화 구성

증빙:
- Docker 빌드/실행 결과: `<사진>`
- GitHub Actions CI 성공 화면: `<사진>`

### LV5. CD + ALB + ASG + HTTPS
- ECR 이미지 배포
- Launch Template + ASG + Target Group + ALB 구성
- 도메인 연결 및 HTTPS(443) 적용

증빙:
- Target Group 정상(Healthy) 화면: `<사진>`
- ALB 리스너(80->443, 443->TG) 설정 화면: `<사진>`
- `https://api.devnest.click/actuator/health` 성공 화면: `<사진>`

### LV6. CloudFront
- S3 객체를 CloudFront 도메인으로 접근 가능하도록 설정
- OAC 기반 버킷 정책 적용

증빙:
- CloudFront 배포 설정 화면: `<사진>`
- S3 버킷 정책(OAC) 화면: `<사진>`
- CloudFront URL 이미지 접근 성공 화면: `<사진>`

## 6. API 테스트 예시 (Postman)

### 회원 생성
- `POST https://api.devnest.click/api/members`
```json
{
  "name": "lv6-test-user",
  "email": "lv6-test@example.com"
}
```

### 프로필 이미지 업로드
- `POST https://api.devnest.click/api/members/{memberId}/profile-image`
- Body: `form-data`
- key: `profileImage` (File)

### 프로필 이미지 조회
- `GET https://api.devnest.click/api/members/{memberId}/profile-image`

### 헬스체크
- `GET https://api.devnest.click/actuator/health`

## 7. 트러블슈팅

### 1) ALB 502/504
- 원인: Target Group 비정상, SG/헬스체크 경로 불일치
- 조치: TG 경로 `/actuator/health`, 포트 `8080`, ALB SG -> EC2 SG 8080 허용

증빙: `<사진>`

### 2) EC2에서 DB 연결 타임아웃
- 원인: RDS 접근 보안그룹 규칙 누락
- 조치: RDS SG 인바운드 `3306` 소스를 EC2 SG로 허용

증빙: `<사진>`

### 3) S3 업로드 403 (PutObject 권한 오류)
- 원인: `ci-cloud-ec2-role`에 S3 PutObject 권한 미부여
- 조치: `arn:aws:s3:::camp-cloud-dorayaki-files/members/*` 리소스에 `s3:PutObject`, `s3:GetObject` 허용

증빙: `<사진>`

## 8. 최종 확인
- API 정상 응답: 확인 완료
- 이미지 업로드/조회: 확인 완료
- ALB/ASG/HTTPS: 확인 완료
- CloudFront 접근: 확인 완료

최종 결과 캡처: `<사진>`
