# Spring-boots : 신발 판매 쇼핑몰


<img src="/uploads/5ca2093e2e0d072fc764d88d1693523a/image.png" alt="왜안되징" width="200px" /> 

## 프로젝트 소개
Spring-boots는 사용자가 신발을 편리하게 구매할 수 있도록 다양한 기능을 제공하는 웹 기반 쇼핑몰입니다. 관리자 기능, 상품 카테고리 분류, 장바구니 시스템, 주문 기능을 지원하며, 소셜 로그인 및 JWT 기반 보안 인증을 제공합니다.



## 팀원 소개
| 이름   | 역할     | 
|--------|---------|
| 이서율 | Order    | 
| 윤호준 | Cart     | 
| 고범석 | Category | 
| 이찬진 | User     | 
| 차현승 | Product  |


## ERD
<img src="/uploads/02566675f63487a3de55101f5fefb7fe/image.png" />


## 기술 스택
- **Frontend**: Bulma:0.9.3, Javascript
- **Backend**: Spring Boot:3.3.4, JDK 22, Mockito:5.14.1, Junit: 5.11.3
- **Database**: MySQL:8.0.35 (AWS,RDS)
- **Security**: Spring Security:3.3.4, JWT
- **DevOps**: 
- **Server**: AWS EC2, GCP VM<br>
            - OS: Ubuntu 24.04 LTS(EC2) / Ubuntu 20.04.6 LTS(GCP)<br>
            - JRE: OpenJDK 22<br>
            - 애플리케이션 서버: Spring Boot (내장 Tomcat 사용)




## 주요 기능
- **사용자**: 회원가입, 로그인, 로그아웃, 구글 소셜 로그인, 관리자 계정 전환 기능 제공
- **카테고리**: 상품 카테고리 등록, 수정, 삭제 기능 제공, 카테고리 검색, 이벤트 생성 및 활성화 이벤트 확인 기능 제공
- **상품**: 상품 조회, 상품 상세 페이지, 상품 등록/수정/삭제 기능 제공
- **장바구니**: 장바구니 조회, 수량/사이즈 변경, 선택 삭제/ 전체 삭제/ 개별 삭제 기능 제공
- **주문**: 결제 정보 시 주소지 자동 입력, 회원/관리자 별 주문 조회 및 취소 기능 제공

## 배포 환경
```
- 데이터베이스: AWS RDS (MySQL)
    - MySQL 버전: 8.0.35
- 파일 저장소: AWS S3
- 기타 라이브러리 및 도구
    - AWS CLI: AWS 명령줄 인터페이스 v2
    - Postman (API 테스트), Lombok
```





## API 명세서

### 회원  

| **MVP**        | **Method** | **URI**                               | **Description**                              | **Cookie**            | **Request Body**                                                                                                                             | **Response**                                                                                                                                                                                                 |
|----------------|------------|---------------------------------------|----------------------------------------------|-----------------------|----------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| user (비회원)   | POST       | /api/login                            | 로그인                                       |                       | JwtTokenLoginRequestDto { “userRealId” : “string”, “password” : “string” }                                                                   | 200 OK : 인증 성공, JWT 리프레쉬토큰, 엑세스 토큰 반환 <br> 401 Unauthorized : 인증 실패                                                                                                                     |
| user (비회원)   | POST       | /api/signup                           | 회원가입                                     |                       | UserSignupRequestDto { “username” : “string”, “userRealId” : “string”, “password” : “string”, “email” : “string” }                           | 201 Created : 회원 가입 완료 <br> { “message” : “성공적으로 회원가입하셨습니다.” } <br> 400 Bad Request : 잘못된 요청 데이터 <br> { “message” : “잘못된 요청입니다.” }                                          |
| user (회원)     | GET        | /api/users-info                       | 개인 정보 조회(인증된 토큰 발급 시)            | accessToken : {user_token} <br> refreshToken : {user_token} |                                                                                                                      | 200 OK : 개인정보조회 <br> { “userId” : “Long”, “username” : “String”, “userRealId” : “String”, “email” : “String”, “role” : “String”, “createdAt” : “String”, “userInfoList” : { “address” : “String”, “streetAddress” : “String”, “detailedAddress” : “String”, “phone” : “String” } } <br> 404 NOT_FOUND : 인증정보를 불러올 수 없음 <br> 400 BAD_REQUEST : 잘못된 요청 |
| user (회원)     | PATCH      | /api/users/{userInfoId}               | 개인 정보 수정 (이름, 아이디는 변경 불가능)    | accessToken : {user_token} <br> refreshToken : {user_token} | UserUpdateRequestDto { “currentPassword” : “String”, “updatePassword” : “String”, “email” : “string”, “address” : { “address” : “String”, “streetAddress” : “String”, “detailedAddress” : “String”, “phone” : “String” } } | 200 OK : 회원정보 수정 완료 <br> { “message” : “정상적으로 수정되었습니다.” } <br> 400 Bad Request : 잘못된 데이터 요청 <br> { “message” : “잘못된 데이터 요청입니다.” }                                       |
| user (회원)     | DELETE     | /api/users-soft/{id}                  | 회원 탈퇴 (soft-delete)                       | accessToken : {user_token} <br> refreshToken : {user_token} |                                                                                                                      | 200 OK : 회원 탈퇴 완료 <br> { “message” : “회원탈퇴 성공” } <br> 400 Bad Request : 탈퇴 실패 <br> { “message” : “오류 발생” }                                                                                   |
| user (회원)     | POST       | /api/users/check-password             | 비밀번호 확인 (회원 정보 삭제)                 |                       | UserPasswordRequestDto { “password” : “string” }                                                                                              | 200 OK : 비밀번호 확인 완료 <br> 401 Unauthorized : 인증 실패                                                                                                                                                 |
| user (회원)     | POST       | /api/logout                           | 로그아웃                                      |                       |                                                                                                                      | 200 OK : 로그아웃 <br> 401 Unauthorized : 권한 없음                                                                                                                                                          |
| user (비회원)   | GET        | /api/signup/checkId?user_real_id={user_real_id} | 아이디 중복확인 (회원가입 시)                  |                       |                                                                                                                      | 200 OK : 중복체크 완료 <br> { “is_available” : true, “message” : “사용할 수 있는 아이디입니다.” } <br> 409 Conflict : { “is_available” : false, “message” : “이미 사용 중인 아이디입니다.” }                 |
| admin          | GET        | /api/admin/users                      | 모든 회원 정보 조회                          | accessToken : {admin_token} <br> refreshToken : {admin_token} |                                                                                                                      | 200 OK : 모든 사용자 정보 반환 (JSON) <br> List<AllUsersInfoResponseDto> { “userId” : “Long”, “userRealId” : “String”, “created_at” : “string”, “email” : “string”, “role” : “string”, “provider” : “string”, “username” : “string” }, … <br> 403 Forbidden : 관리자 권한 필요 <br> 500 Internal Server Error : 서버 오류 발생                  |
| admin          | GET        | /api/admin/users/{user_id}            | 특정 회원 정보 조회                          | accessToken : {admin_token} <br> refreshToken : {admin_token} |                                                                                                                      | 200 OK : 특정 사용자 정보 반환 (JSON) <br> UserInfoResponseDto { “created_at” : “string”, “email” : “string”, “role” : “string”, “provider” : “string”, “username” : “string” } <br> 404 Not Found : 해당 사용자 찾을 수 없음                                                                 |
| admin          | GET        | /api/users/admin-check                | 관리자 확인 API                              | accessToken : {admin_token} <br> refreshToken : {admin_token} |                                                                                                                      | 401 Unauthorized : 인증되지 않은 사용자 <br> { “message” : “현재 엑세스 토큰이 없습니다.” } <br> 200 OK : 인증 성공 <br> { “message” : “관리자 인증 성공” } <br> 403 Forbidden : 인증 실패 <br> { “message” : “관리자 인증 실패” } |
| admin          | POST       | /api/users/grant                      | 관리자 코드 체크 API                          | accessToken : {admin_token} <br> refreshToken : {admin_token} |                                                                                                                      | 200 OK : 인증 성공 <br> { “message” : “success” } <br> 401 Unauthorized : 인증 실패 <br> { “message” : “fail” }                                                                                              |


 ### 상품

| **MVP** | **Method** | **URI**                              | **Description**                         | **Request Body**                                                                                                                                      | **Request Params**                                                                                                                                                                           | **Response**                                                                                                                       |
|---------|------------|--------------------------------------|-----------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------|
| Item    | POST       | /api/admin/items                     | 관리자 제품 추가                         | { “id” : “Long”, “item_name” : “string”, “category_id” : “Long”, “item_maker” : “string”, “item_price” : “int”, “item_description” : “string”, “item_color” : “string”, “item_size” : “int”, “created_at” : “datetime”, “updated_at” : “datetime”, “image_url” : “string”, “keywords” : “List<string>” } | @ModelAttribute CreateItemDto requestItemDto, @RequestParam("file") MultipartFile file                                                                  | 200 OK : { “message” : “등록이 완료되었습니다.” } <br> 400 Bad Request : { “error” : “내용이 충분하지 않습니다.” }                     |
| Item    | PUT        | /api/items/{items_id}                | 제품 수정                               | { “id” : “Long”, “item_name” : “string”, “category_id” : “Long”, “item_maker” : “string”, “item_price” : “int”, “item_description” : “string”, “item_color” : “string”, “item_size” : “int”, “created_at” : “datetime”, “updated_at” : “datetime”, “image_url” : “string”, “keywords” : “List<string>” } | @PathVariable("itemId") Long id, @ModelAttribute UpdateItemDto updateItemDto                                                                             | 200 OK : { “message” : “수정이 완료되었습니다.” } <br> 400 Bad Request : { “error” : “내용이 충분하지 않습니다.” }                     |
| Item    | GET        | /api/items/{items_id}                | 제품 상세보기                            |                                                                                                                                                       | @PathVariable("itemId") Long id                                                                                                                        | 200 OK : 제품 상세보기 완료 <br> 410 Gone : { “error” : “제품이 더이상 존재하지 않습니다.” }                                           |
| Item    | DELETE     | /api/items/{items_id}                | 제품 삭제                               |                                                                                                                                                       | @PathVariable("itemId") Long id                                                                                                                        | 200 OK : { “message” : “제품 삭제를 완료했습니다.” }                                                                                   |
| Item    | GET        | /api/items/categories/{category_id}  | 카테고리 ID별 상품 조회                  |                                                                                                                                                       | @PathVariable("category_id") Long categoryId, @RequestParam(required = false, defaultValue = "default") String sort, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "8") int limit |                                                                                                                                   |
| Item    | GET        | /api/items/thema/{thema}             | 특정 테마에 속하는 상품 조회             |                                                                                                                                                       | @PathVariable("thema") String thema, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "8") int limit, @RequestParam(defaultValue = "default") String sort             |                                                                                                                                   |
| Item    | GET        | /api/items/search                    | 특정 키워드를 갖는 상품 조회             |                                                                                                                                                       | @RequestParam String keyword, @RequestParam(required = false) String sort, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "8") int limit                          |                                                                                                                                   |
| Item    | GET        | /api/items/list/search/name          | 상품 목록 페이지에서 특정 상품명을 갖는 상품 조회 |                                                                                                                                                       | @RequestParam String itemName, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size                                 |                                                                                                                                   |
| Item    | GET        | /api/items                           | 상품 전체 조회                           |                                                                                                                                                       | @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size                                                                                                        |                                                                                                                                   |


### 카테고리 / 이벤트

| **MVP** | **Method** | **URI** | **Description** | **Request Body** | **Request Params** | **Response** |
|---------|------------|---------|-----------------|------------------|-------------------|--------------|
| category | POST | /api/admin/categories | 관리자 - 새 카테고리 추가 | CategoryRequestDto { "categoryName": "string", "categoryThema": "string", "categoryContent": "string", "displayOrder": "int" } | | 201 Created: 생성된 카테고리 정보 반환<br>400 Bad Request:<br>{ "errorCode": "필수_파라미터_누락", "errorMessage": "카테고리 이름은 필수 항목입니다." }<br>401 Unauthorized: 인증 실패 |
| category | PATCH | /api/admin/categories/{category_id} | 관리자 - 카테고리 정보 수정 | CategoryRequestDto { "categoryName": "string", "categoryThema": "string", "categoryContent": "string", "displayOrder": "int" } | category_id (PathVariable) | 200 OK: 수정된 카테고리 정보 반환<br>404 Not Found: 카테고리가 존재하지 않음<br>400 Bad Request:<br>{ "errorCode": "파라미터_길이_초과", "errorMessage": "카테고리 이름은 50자를 초과할 수 없습니다." }<br>401 Unauthorized: 인증 실패 |
| category | DELETE | /api/admin/categories/{category_id} | 관리자 - 카테고리 삭제 | | category_id (PathVariable) | 204 No Content: 삭제 성공<br>404 Not Found:<br>{ "errorCode": "리소스_없음", "errorMessage": "삭제할 카테고리를 찾을 수 없습니다." }<br>401 Unauthorized:<br>{ "errorCode": "권한_없음", "errorMessage": "카테고리 삭제 권한이 없습니다." } |
| category | GET | /api/categories/themas | 모든 카테고리 테마 목록 조회 | | | 200 OK: 카테고리 테마 목록 반환 |
| category | GET | /api/categories/themas/{thema} | 특정 테마의 카테고리 목록 조회 | | category_thema (PathVariable) | 200 OK: 카테고리 테마 목록 반환<br>404 Not Found: 카테고리 테마가 존재하지 않음 |
| category | GET | /api/categories/{category_id} | 카테고리 상세 조회 | | category_id (PathVariable) | 200 OK: 카테고리 상세 정보 반환<br>{ "id": "Long", "categoryName": "string", "categoryContent": "string", "categoryThema": "string", "imageUrl": "string", "subcategories": [ { "id": "Long", "categoryName": "string" } ] }<br>404 Not Found: 카테고리가 존재하지 않음 |
| category | GET | /api/categories/themas/displayOrder/{theme} | displayOrder '0'을 제외한 나머지 카테고리 출력 | | theme (PathVariable) | 200 OK: displayOrder가 0이 아닌 카테고리 목록 반환<br>[ { "id": "Long", "categoryName": "string", "categoryThema": "string", "categoryContent": "string", "displayOrder": "int", "imageUrl": "string" } ]<br>404 Not Found: 카테고리 테마가 존재하지 않음 |
| category | GET | /api/admin/categories | 관리자 카테고리 전체 목록 조회 (페이지네이션) | | page (defaultValue = "0"), limit (defaultValue = "10") | 200 OK: 모든 카테고리 목록 반환<br>{ "content": [ { "id": "Long", "categoryName": "string", "categoryThema": "string", "categoryContent": "string", "displayOrder": "int", "createdAt": "LocalDateTime", "updatedAt": "LocalDateTime" } ], "pageable": { "pageNumber": "int", "pageSize": "int" }, "totalElements": "Long", "totalPages": "int" }<br>400 Bad Request:<br>{ "errorCode": "잘못된_파라미터_형식", "errorMessage": "페이지당 항목 수는 1에서 10 사이여야 합니다." }<br>401 Unauthorized: 인증 실패 |
| event | GET | /api/events | 종료되지 않은 이벤트 목록 조회 (페이지네이션) | | page (defaultValue = "0"), limit (defaultValue = "10") | 200 OK: 활성화된 이벤트 목록<br>{ "content": [ { "id": "Long", "eventTitle": "string", "thumbnailImageUrl": "string", "startDate": "LocalDate", "endDate": "LocalDate", "status": "string" } ], "pageable": { "pageNumber": "int", "pageSize": "int" }, "totalElements": "Long", "totalPages": "int" }<br>400 Bad Request:<br>{ "errorCode": "잘못된_파라미터_형식", "errorMessage": "페이지당 항목 수는 1에서 10 사이여야 합니다." } |
| event | GET | /api/events/{event_id} | 이벤트 상세 조회 | | event_id (PathVariable) | 200 OK: 이벤트 상세 정보 반환<br>{ "id": "Long", "eventTitle": "string", "eventContent": "string", "thumbnailImageUrl": "string", "contentImageUrl": ["string"], "startDate": "LocalDate", "endDate": "LocalDate", "isActive": "boolean" }<br>404 Not Found: 이벤트가 존재하지 않음 |
| event | POST | /api/admin/events | 새 이벤트 생성 | { "title": "string", "content": "string", "start_date": "date", "end_date": "date" } | | 201 Created: 생성된 이벤트 정보 반환<br>{ "id": "Long", "eventTitle": "string", "eventContent": "string", "thumbnailImageUrl": "string", "contentImageUrl": ["string"], "startDate": "LocalDate", "endDate": "LocalDate", "isActive": "boolean" }<br>400 Bad Request:<br>{ "errorCode": "유효하지_않은_날짜", "errorMessage": "이벤트 종료일은 시작일 이후여야 합니다." }<br>401 Unauthorized: 인증 실패 |
| event | PATCH | /api/admin/events/{event_id} | 이벤트 정보 수정 | { "title": "string", "content": "string", "start_date": "date", "end_date": "date" } | event_id (PathVariable) | 200 OK: 수정된 이벤트 정보 반환<br>{ "id": "Long", "eventTitle": "string", "eventContent": "string", "thumbnailImageUrl": "string", "contentImageUrl": ["string"], "startDate": "LocalDate", "endDate": "LocalDate", "isActive": "boolean" }<br>404 Not Found: 이벤트가 존재하지 않음<br>400 Bad Request:<br>{ "errorCode": "파라미터_길이_초과", "errorMessage": "이벤트 제목은 100자를 초과할 수 없습니다." }<br>401 Unauthorized: 인증 실패 |
| event | DELETE | /api/admin/events/{event_id} | 이벤트 삭제 | | event_id (PathVariable) | 204 No Content: 삭제 성공<br>404 Not Found: 이벤트가 존재하지 않음<br>401 Unauthorized: 인증실패 |
| event | GET | /api/admin/events | 관리자 이벤트 전체 목록 조회 | | page (defaultValue = "0"), limit (defaultValue = "10") | 200 OK: 모든 이벤트 목록<br>{ "content": [ { "id": "Long", "eventTitle": "string", "eventContent": "string", "startDate": "LocalDate", "endDate": "LocalDate", "isActive": "boolean", "status": "string", "updatedAt": "LocalDateTime" } ], "pageable": { "pageNumber": "int", "pageSize": "int" }, "totalElements": "Long", "totalPages": "int" }<br>400 Bad Request:<br>{ "errorCode": "잘못된_파라미터_형식", "errorMessage": "페이지당 항목 수는 1에서 10 사이여야 합니다." }<br>401 Unauthorized: 인증실패 |
| event | GET | /api/admin/events/{event_id} | 관리자 개별 이벤트 조회 | | event_id (PathVariable) | 200 OK: 이벤트 관리자 상세 정보<br>{ "id": "Long", "eventTitle": "string", "eventContent": "string", "startDate": "LocalDate", "endDate": "LocalDate", "isActive": "boolean", "status": "string", "updatedAt": "LocalDateTime" }<br>404 Not Found: 이벤트가 존재하지 않음<br>401 Unauthorized: 인증실패 |




### 주문

| MVP     | Method                     | URI                           | 설명                                                                                                     | 요청 본문                                                                                                                                             | 요청 파라미터 | 응답                                                                                                                                                              |
|---------|----------------------------|-------------------------------|---------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------|----------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **order** | `GET`                       | `/api/orders`                | 사용자의 주문 목록을 조회합니다.                                                                         |                                                                                                                                                      |                | 응답: 200 OK: 조회 완료.<br>401 Unauthorized: 사용자 인증이 필요함.<br>404 Not Found: 주문을 찾을 수 없는 경우.<br><br>응답 본문: <br>[ { "orders_id": "int", "created_at": "datetime", "orders_total_price": "int", "order_status": "string", "shipping_address": "string", "delivery_fee": "int", "quantity": "int", "items": [ { "item_name": "string", "orderitems_quantity": "int", "orderitems_total_price": "int" } ] } ] |
| **order** | `GET`                       | `/api/orders/{orders_id}`    | 특정 주문 번호(orders_id)에 해당하는 주문의 상세 정보를 조회합니다.                                       |                                                                                                                                                      | `orders_id`    | 응답: 200 OK: 주문 상세 조회 완료.<br>404 Not Found: 주문 ID가 존재하지 않는 경우.<br>401 Unauthorized: 사용자 인증이 필요함.<br><br>응답 본문: <br>{ "orders_id": "int", "created_at": "datetime", "orders_total_price": "int", "order_status": "string", "shipping_address": "string", "recipient_name": "string", "recipient_contact": "string", "delivery_fee": "int", "quantity": "int", "items": [ { "item_name": "string", "orderitems_quantity": "int", "orderitems_total_price": "int", "item_image": "string" } ] } |
| **order** | `POST`                      | `/api/orders`                | 사용자가 장바구니를 바탕으로 새 주문을 추가합니다.                                                     | { "user_id": "int", "shipping_address": "string", "recipient_name": "string", "recipient_contact": "string", "delivery_message": "string", "items": [ { "item_id": "int", "item_quantity": "int", "item_size": "int" } ] } |                | 응답: 201 Created: 주문 추가.<br>400 Bad Request: 요청 본문이 잘못됨 (수취인 정보, 배송 정보 누락, 주문 상품이 없음).<br>401 Unauthorized: 사용자 인증이 필요함.<br><br>응답 본문: <br>{ "orders_id": "int", "status": "주문이 성공적으로 생성되었습니다." } |
| **order** | `PUT`                       | `/api/orders/{orders_id}`    | 사용자가 주문을 수정합니다 (배송 시작 전까지 가능).                                                    | { "shipping_address": "string", "recipient_name": "string", "recipient_contact": "string" }                                                       | `orders_id`    | 응답: 200 OK: 주문 수정 완료.<br>404 Not Found: 주문 ID가 존재하지 않는 경우.<br>403 Forbidden: 사용자가 권한이 없는 경우.<br><br>응답 본문: <br>{ "orders_id": "int", "status": "주문이 성공적으로 수정되었습니다." } |
| **order** | `DELETE`                    | `/api/orders/{orders_id}`    | 사용자가 배송 전 주문을 취소합니다.                                                                     |                                                                                                                                                      | `orders_id`    | 응답: 200 OK: 주문 취소 완료.<br>404 Not Found: 주문 ID가 존재하지 않는 경우.<br>403 Forbidden: 사용자가 권한이 없는 경우.<br><br>응답 본문: <br>{ "orders_id": "int", "status": "주문이 성공적으로 취소되었습니다." } |
| **order** | `GET`                       | `/api/admin/orders`          | 관리자가 모든 주문을 조회합니다.                                                                         |                                                                                                                                                      |                | 응답: 200 OK: 조회 완료.<br>401 Unauthorized: 인증이 필요한 경우.<br>403 Forbidden: 관리자가 아닌 사용자가 접근한 경우.<br>404 Not Found: 주문을 찾을 수 없는 경우.<br><br>응답 본문: <br>[ { "orders_id": "int", "user_id": "int", "created_at": "datetime", "orders_total_price": "int", "order_status": "string", "shipping_address": "string", "recipient_name": "string", "recipient_contact": "string", "delivery_fee": "int" } ] |
| **order** | `PATCH`                     | `/api/admin/orders/{orders_id}/status` | 관리자가 주문 상태를 수정합니다.                                                                       | { "orders_status": "string" }                                                                                                                        | `orders_id`    | 응답: 200 OK: 주문 상태 수정 완료.<br>400 Bad Request: 요청 본문이 잘못된 경우.<br>404 Not Found: 주문 ID가 존재하지 않는 경우.<br>403 Forbidden: 관리자가 아닌 사용자가 접근한 경우.<br><br>응답 본문: <br>{ "orders_id": "int", "status": "주문 상태가 성공적으로 수정되었습니다." } |
| **order** | `DELETE`                    | `/api/admin/orders/{orders_id}` | 관리자가 주문을 삭제합니다.                                                                             |                                                                                                                                                      | `orders_id`    | 응답: 200 OK: 삭제 완료.<br>404 Not Found: 주문 ID가 존재하지 않는 경우.<br>403 Forbidden: 관리자가 아닌 사용자가 접근한 경우.<br><br>응답 본문: <br>{ "orders_id": "int", "status": "주문이 성공적으로 삭제되었습니다." } |


## 트러블슈팅

배포 환경에서 item 엔티티에서 버그가 발생하는 문제
    - 문제 분석: 로컬 H2 데이터베이스에서 배포 환경의 MySQL 데이터베이스로 전환하는 과정에서, Item 엔티티
의 List<String> 형태의 필드(itemColor)가 제대로 저장되지 않는 문제가 발생

    - 해결 방법: Á StringListConverter 클래스를 만들어 JPA에서 리스트 데이터를
문자열로 변환하고, 다시 리스트로 변환하는 로직을 추À

Á List<String> 데이터를 콤마로 구분된 문자열로 변환한 뒤 MySQL에 저장하고, 다시 엔티티로 불러올 때는 콤마로 구분된 문자열을 리스트로 변환하도록 설정


카테고리 삭제 시 순환 참조와 참조 무결성 제약으로 인한 오류
    - 문제 분석: 카테고리 관리자 페이지에서 카테고리를 삭제하는 과정에서 카테고리와 상품 간 양방향 매핑으로 순환참조와 참조 무결성 제약으로 인한 문제 발생

    - 순환참조 문제를 @JsonIgnore 방법을 사용하지 않고 단방향 관계로 설정하여 해결, 직접적으로 삭제 로직을 통해 카테고리 삭제 전에 상품의 이미지를 S3에서 삭제하고 상품 주문 정보, 상품, 카테고리 순으로 제거하도록 처리
