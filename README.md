# 클라우드 과제 제출 README (LV0 ~ LV6)

## 0. 제출 정보
- TIL(트러블슈팅) 링크: `<https://dorayaki.tistory.com/51>`
- 구현 단계: `LV6`
- 어려웠던 점/고민한 점: `<전부>`

---

## 1. 서비스 개요
- 팀원 정보 저장/조회 API
- 프로필 이미지 업로드 및 조회 API
- AWS 기반 Stateless 아키텍처로 배포

배포 주소:
- API 도메인: `https://api.devnest.click`
- Health Check: `https://api.devnest.click/actuator/health`
- CloudFront 도메인: `https://d1y67ty5km7ay2.cloudfront.net`

---

## LV0 - AWS Budget 설정 (필수)
### 수행 내용
- AWS Budgets 월 예산 `$100` 설정
- 예산 80% 도달 시 이메일 알림 설정

### 증빙
- AWS Budgets 설정 완료 화면: `<사진>`

---

## LV1 - 네트워크 구축 및 핵심 기능 배포 (필수)
### 인프라
- VPC 구성 및 Public/Private Subnet 분리
- Public Subnet에 EC2 생성

### 애플리케이션
- `POST /api/members` 구현 (이름, 나이, MBTI 저장)
- `GET /api/members/{id}` 구현 (팀원 조회)
- Profile 분리 (`local: H2`, `prod: MySQL`)
- 요청 로깅/예외 로깅 구현 (`INFO` / `ERROR`)
- Actuator 추가 및 health 노출

### 검증
- 애플리케이션 배포 후 health 확인

### 제출 요구사항
- EC2 Public IP: `<EC2 Public IP>`
- 관련 증빙: `<사진>`

---

## LV2 - DB 분리 및 보안 연결 (필수)
### 인프라
- Public Subnet에 MySQL RDS 생성
- 보안 그룹 체이닝: RDS 인바운드 소스 = EC2 보안그룹 ID
- Parameter Store에 DB 정보 + 확인용 파라미터 저장

### 애플리케이션
- Parameter Store 기반 설정 주입 후 RDS 연결
- `/actuator/info`에서 Parameter Store의 `team-name` 출력

### 제출 요구사항
1. Actuator info URL: `<http://.../actuator/info>`
2. RDS 보안 그룹 인바운드 화면(EC2 SG ID 소스 확인): `<사진>`
3. `/actuator/info` 응답 화면: `<사진>`

---

## LV3 - 프로필 사진 기능 + 권한 관리 (필수)
### 인프라
- S3 버킷 생성 (모든 퍼블릭 액세스 차단)
- S3 접근 IAM Role/Policy 구성 후 EC2 연결

### 애플리케이션
- `POST /api/members/{id}/profile-image` (Multipart 업로드 + DB URL/키 반영)
- `GET /api/members/{id}/profile-image` (Presigned URL 발급)
- Presigned URL 유효기간 7일 설정

### 제출 요구사항
- Presigned URL 1개: `<URL>`
- 만료 시간: `<expiresAt>`
- IAM Role 방식 제출 시 접근 성공 스크린샷: `<사진>`

---

## LV4 - Docker & CI/CD 파이프라인 (도전)
### 수행 내용
- Dockerfile 작성 및 이미지 빌드
- Github Actions `deploy.yml` 구성
  - CI: main push 시 build/test
  - CD: Docker Hub push + EC2 pull/run

### 검증
- 코드 push 시 서버 자동 반영 확인

### 제출 요구사항
1. Github Actions 성공 화면: `<사진>`
2. EC2 `sudo docker ps` 실행 화면: `<사진>`

---

## LV5 - 고가용성 + 보안 도메인 (ALB + ASG + HTTPS) (도전)
### 인프라
- NAT Gateway 생성 및 Private Route Table 연결
- RDS/EC2를 Private Subnet 환경으로 구성
- Route53 도메인 구매/연결
- ACM 인증서 발급
- ALB 구성
  - 80 -> 443 리다이렉트
  - 443 리스너에 인증서 적용
- ASG 구성 (Launch Template + Target Group 연결)

### 제출 요구사항
- HTTPS 적용 도메인 URL: `https://api.devnest.click`
- Target Group Registered targets Healthy 화면: `<사진>`
- ALB 리스너 설정 화면: `<사진>`

---

## LV6 - CloudFront CDN (도전)
### 수행 내용
- S3 원본 기반 CloudFront 배포 생성
- OAC 기반 S3 접근 정책 적용
- 이미지를 CloudFront 도메인으로 조회

### 제출 요구사항
- CloudFront 이미지 URL 1개 (필수 형식: `https://dxxxxxxx.cloudfront.net/...`)
  - `https://d1y67ty5km7ay2.cloudfront.net/members/4/profile/7448e26c-4804-4703-ac44-c9c20288ae6a-Screenshot_250930_005258.jpg`
- CloudFront 이미지 접근 성공 화면: `<사진>`

---

## 2. API 테스트 기록 (Postman)
### 2.1 팀원 생성
`POST https://api.devnest.click/api/members`
```json
{
  "name": "lv6-test-user",
  "age": 25,
  "mbti": "INTJ"
}
```

### 2.2 팀원 조회
`GET https://api.devnest.click/api/members/{id}`

### 2.3 프로필 이미지 업로드
`POST https://api.devnest.click/api/members/{id}/profile-image`
- Body: form-data
- key: `profileImage` (File)

### 2.4 프로필 이미지 조회
`GET https://api.devnest.click/api/members/{id}/profile-image`

### 2.5 헬스 체크
`GET https://api.devnest.click/actuator/health`

증빙:
- Postman 요청/응답 화면: `<사진>`

---

## 3. 트러블슈팅 요약
### 이슈 1) ALB 504 / TargetGroup Unhealthy
- 원인: Health check 경로/포트/SG 불일치
- 조치: TG `/actuator/health` + 8080, ALB SG <-> EC2 SG 규칙 정정
- 증빙: `<사진>`

### 이슈 2) DB 연결 실패 (Communications link failure)
- 원인: RDS 보안 그룹 및 네트워크 경로 문제
- 조치: RDS 인바운드 3306 소스를 EC2 SG로 설정
- 증빙: `<사진>`

### 이슈 3) S3 업로드 403 (PutObject 권한 없음)
- 원인: `ci-cloud-ec2-role`의 S3 PutObject 권한 누락
- 조치: `arn:aws:s3:::camp-cloud-dorayaki-files/members/*` 대상 Put/Get 권한 부여
- 증빙: `<사진>`

### 이슈 4) CloudFront URL 접근 실패
- 원인: 객체 키 경로 오입력 또는 DNS/배포 반영 대기
- 조치: 실제 S3 key 기준으로 CloudFront URL 구성 후 재검증
- 증빙: `<사진>`

---

## 4. 최종 검증 체크리스트
- [x] LV0 Budget 설정 완료
- [x] LV1 API + EC2 배포 완료
- [x] LV2 Parameter Store + RDS 연결 완료
- [x] LV3 S3 업로드 + Presigned URL 완료
- [x] LV4 Docker + CI/CD 완료
- [x] LV5 HTTPS 도메인 + ALB/ASG 완료
- [x] LV6 CloudFront 이미지 조회 완료

최종 검증 캡처: `<사진>`
