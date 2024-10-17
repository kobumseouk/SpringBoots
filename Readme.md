# Spring-boots : 신발 판매 쇼핑몰

## 프로젝트 소개!
Spring-boots는 사용자가 신발을 편리하게 구매할 수 있도록 다양한 기능을 제공하는 웹 기반 쇼핑몰입니다. 관리자 기능, 상품 카테고리 분류, 장바구니 시스템, 주문 기능을 지원하며, 소셜 로그인 및 JWT 기반 보안 인증을 제공합니다.

## 팀원 소개
| 이름   | 역할     | 담당 기능                        |
|--------|---------|----------------------------------|
| 이서율 | Order    | 주문 시스템 |
| 윤호준 | Cart     | 장바구니 도메인 |
| 고범석 | Category | 카테고리 도메인   |
| 이찬진 | User     | 회원 도메인   |
| 차현승 | Product  | 상품 도메인   |

<aside>
<img src="/icons/code_green.svg" alt="/icons/code_green.svg" width="40px" /> **필수 요구사항**

- **회원 도메인 (`User`) - 찬진**
- **카테고리 도메인 (`Category`) - 범석**
- **상품 도메인 (`Product`) - 현승**
- **장바구니 도메인 (`orderItem`) - 호준**
- **주문 도메인 (`Order`) - 서율**
</aside>

## ERD
- [ERD 링크](https://www.erdcloud.com/d/7RXXuJrNBwNyYMd7s)

## 기술 스택
- **Frontend**: Bulma, Javascript
- **Backend**: Spring Boot
- **Database**: MySQL, H2
- **Security**: Spring Security, JWT(JSON Web Token)
- **DevOps**: AWS (EC2, S3, RDS)..?

## 주요 기능
- **사용자**: 회원가입, 로그인, 로그아웃, 소셜 로그인, 관리자 계정 전환
- **카테고리**: 상품 카테고리 등록, 수정, 삭제 기능 제공
- **상품**: 상품 조회, 상품 상세 페이지, 상품 등록/수정/삭제 기능 제공
- **장바구리**: 장바구니 표시, 가격 계산
- **주문**: 결제 정보 입력, 주문 내역 조회

## 배포 환경
```
- 서버 : AWS EC2, GCP VM
    - OS: Ubuntu 24.04 LTS(EC2) / Ubuntu 20.04.6 LTS(GCP)
    - JRE: OpenJDK 22
    - 애플리케이션 서버: Spring Boot (내장 Tomcat 사용)
- 데이터베이스: AWS RDS (MySQL)
    - MySQL 버전: 8.0.35
- 파일 저장소: AWS S3
- 기타 라이브러리 및 도구
    - AWS CLI: AWS 명령줄 인터페이스 v2
```

## 개발 환경
```
- 운영체제: Windows 11, 
- IDE: IntelliJ IDEA
- 빌드 도구: Gradle
- JDK 버전: JDK 17
- 버전 관리: Git / Gitlab
- 기타 툴: Postman (API 테스트), Lombok
```

## 의존성 목록
### 주요 라이브러리 및 버전
- **Spring Boot**:
    - `spring-boot-starter-data-jpa`
    - `spring-boot-starter-jdbc`
    - `spring-boot-starter-validation:3.2.0`
    - `spring-boot-starter-security`
    - `spring-boot-starter-thymeleaf`
    - `spring-boot-starter-web`
    - `jakarta.validation:jakarta.validation-api`
    -  `org.thymeleaf.extras:thymeleaf-extras-springsecurity6`
    -  `com.h2database:h2`
    -  `org.springframework.boot:spring-boot-starter-test`
    -   `org.springframework.security:spring-security-test`
    -   `org.junit.platform:junit-platform-launcher`

- **보안 및 인증**:
    - `io.jsonwebtoken:jjwt-api:0.11.5`
    - `io.jsonwebtoken:jjwt-impl:0.11.5`
    - `io.jsonwebtoken:jjwt-jackson:0.11.5`
- **Mapper**:
    - `org.mapstruct:mapstruct:1.5.3.Final`
    - `org.mapstruct:mapstruct-processor:1.5.3.Final`
- **Lombok**:
    - `org.projectlombok:lombok`
- **AWS S3 Bucket**:
    -  `org.springframework.cloud:spring-cloud-starter-aws:2.2.6.RELEASE`
    -  `software.amazon.awssdk:bom:2.20.0`
    -  `software.amazon.awssdk:s3`
    -  `software.amazon.awssdk:sts`
- **테스트**:
    - `spring-boot-starter-test`
    - `spring-security-test`
    - `junit-platform-launcher`
- **DevTools**:
    - `spring-boot-devtools`


##테스트##

## API 명세서
https://www.notion.so/elice-track/API-8e1d333563534362bd0cfa5e94440bff
