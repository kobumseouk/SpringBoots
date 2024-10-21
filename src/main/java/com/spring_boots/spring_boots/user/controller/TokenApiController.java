package com.spring_boots.spring_boots.user.controller;

import com.spring_boots.spring_boots.user.domain.UserRole;
import com.spring_boots.spring_boots.user.dto.request.JwtTokenDto;
import com.spring_boots.spring_boots.user.dto.request.JwtTokenLoginRequest;
import com.spring_boots.spring_boots.user.dto.request.RefreshTokenRequest;
import com.spring_boots.spring_boots.user.dto.response.JwtTokenResponse;
import com.spring_boots.spring_boots.user.dto.response.RefreshTokenResponse;
import com.spring_boots.spring_boots.user.exception.PasswordNotMatchException;
import com.spring_boots.spring_boots.user.exception.UserDeletedException;
import com.spring_boots.spring_boots.user.exception.UserNotFoundException;
import com.spring_boots.spring_boots.user.service.TokenService;
import com.spring_boots.spring_boots.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.spring_boots.spring_boots.config.jwt.UserConstants.ACCESS_TOKEN_TYPE_VALUE;
import static com.spring_boots.spring_boots.config.jwt.UserConstants.REFRESH_TOKEN_TYPE_VALUE;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class TokenApiController {

    private final UserService userService;

    //jwt 로그인
    @PostMapping("/login")
    public ResponseEntity<JwtTokenResponse> jwtLogin(
            @RequestBody JwtTokenLoginRequest request,
            HttpServletResponse response,
            @CookieValue(value = "refreshToken", required = false) Cookie existingRefreshTokenCookie
    ) {
        // 기존 쿠키 삭제 로직
        if (existingRefreshTokenCookie != null) {
            deleteTokenCookie(response);
        }

        try {
            JwtTokenDto jwtTokenResponse = userService.login(request);

            getCookie(jwtTokenResponse, response);

            return ResponseEntity.ok().body(JwtTokenResponse
                    .builder()
                    .accessToken(jwtTokenResponse.getAccessToken())
                    .refreshToken(jwtTokenResponse.getRefreshToken())
                    .isAdmin(jwtTokenResponse.getRole().equals(UserRole.ADMIN))
                    .message("로그인 성공")
                    .build());
        } catch (UserNotFoundException | UserDeletedException e) {
            log.info(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(JwtTokenResponse.builder()
                            .message(e.getMessage()).build());
        } catch (PasswordNotMatchException e) {
            log.info(e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(JwtTokenResponse.builder()
                            .message(e.getMessage()).build());
        }
    }

//    //토큰 재발급 로직
//    @PostMapping("/refresh-token")
//    public ResponseEntity<RefreshTokenResponse> refreshAccessToken(@RequestBody RefreshTokenRequest request) {
//        String refreshToken = request.getRefreshToken();
//
//        // refreshToken 검증 및 새로운 accessToken 생성
//        String newAccessToken = tokenService.createNewAccessToken(refreshToken);
//
//        if (newAccessToken == null) {
//            return ResponseEntity.status(401).build(); // 토큰이 유효하지 않은 경우 401 Unauthorized 응답
//        }
//
//        return ResponseEntity.ok(new RefreshTokenResponse(newAccessToken));
//    }

//    //토큰 유효성 api
//    @GetMapping("/protected")
//    public ResponseEntity<String> getProtectedResource(@CookieValue("accessToken") String accessToken) {
//        if (userService.validateToken(accessToken)) {
//            return ResponseEntity.ok("Protected data");
//        } else {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
//        }
//    }

    //엑세스토큰, 리프레시 토큰 쿠키삭제 로직
    private void deleteTokenCookie(HttpServletResponse response) {
        Cookie deleteRefreshTokenCookie = new Cookie(REFRESH_TOKEN_TYPE_VALUE, null);
//            deleteRefreshTokenCookie.setHttpOnly(true); // 자바스크립트에서 접근 불가
        deleteRefreshTokenCookie.setSecure(true); // HTTPS에서만 전송 todo 배포시 설정 주석 해제
        deleteRefreshTokenCookie.setPath("/"); // 동일한 경로
        deleteRefreshTokenCookie.setMaxAge(0); // 쿠키 삭제 설정

        Cookie deleteAccessTokenCookie = new Cookie(ACCESS_TOKEN_TYPE_VALUE, null);
//            deleteAccessTokenCookie.setHttpOnly(true); // 자바스크립트에서 접근 불가
        deleteAccessTokenCookie.setSecure(true); // HTTPS에서만 전송 todo 배포시 설정 주석 해제
        deleteAccessTokenCookie.setPath("/"); // 동일한 경로
        deleteAccessTokenCookie.setMaxAge(0); // 쿠키 삭제 설정

        response.addCookie(deleteRefreshTokenCookie); // 삭제할 쿠키를 response에 추가
        response.addCookie(deleteAccessTokenCookie);
    }

    //쿠키 생성로직
    private void getCookie(JwtTokenDto jwtTokenResponse, HttpServletResponse response) {
        Cookie refreshTokenCookie = new Cookie(
                REFRESH_TOKEN_TYPE_VALUE,
                jwtTokenResponse.getRefreshToken()
        );

        Cookie accessTokenCookie = new Cookie(
                ACCESS_TOKEN_TYPE_VALUE,
                jwtTokenResponse.getAccessToken()
        );

//        refreshTokenCookie.setHttpOnly(true); // 자바스크립트에서 접근할 수 없도록 설정
        refreshTokenCookie.setSecure(true); // HTTPS에서만 전송되도록 설정 (생산 환경에서 사용) todo 배포시 설정 주석 해제
        refreshTokenCookie.setPath("/"); // 쿠키의 유효 경로 설정
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 쿠키의 유효 기간 설정 (예: 7일)

//        accessTokenCookie.setHttpOnly(true); // 자바스크립트에서 접근할 수 없도록 설정
        accessTokenCookie.setSecure(true); // HTTPS에서만 전송되도록 설정 (생산 환경에서 사용) todo 배포시 설정 주석 해제
        accessTokenCookie.setPath("/"); // 쿠키의 유효 경로 설정
        accessTokenCookie.setMaxAge(15 * 60); // 15분

        response.addCookie(refreshTokenCookie);
        response.addCookie(accessTokenCookie);
    }
}
