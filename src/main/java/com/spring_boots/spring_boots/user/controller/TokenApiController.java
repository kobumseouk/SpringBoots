package com.spring_boots.spring_boots.user.controller;

import com.spring_boots.spring_boots.user.domain.UserRole;
import com.spring_boots.spring_boots.user.dto.request.JwtTokenDto;
import com.spring_boots.spring_boots.user.dto.request.JwtTokenLoginRequest;
import com.spring_boots.spring_boots.user.dto.request.RefreshTokenRequest;
import com.spring_boots.spring_boots.user.dto.response.JwtTokenResponse;
import com.spring_boots.spring_boots.user.dto.response.RefreshTokenResponse;
import com.spring_boots.spring_boots.user.service.TokenService;
import com.spring_boots.spring_boots.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TokenApiController {

    private final UserService userService;
    private final TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<JwtTokenResponse> jwtLogin(
            @RequestBody JwtTokenLoginRequest request,
            HttpServletResponse response,
            @CookieValue(value = "refreshToken", required = false) Cookie existingRefreshTokenCookie
    ) {
        // 기존 쿠키 삭제 로직
        if (existingRefreshTokenCookie != null) {
            Cookie deleteRefreshTokenCookie = new Cookie("refreshToken", null);
//            deleteRefreshTokenCookie.setHttpOnly(true); // 자바스크립트에서 접근 불가
//            deleteRefreshTokenCookie.setSecure(true); // HTTPS에서만 전송
            deleteRefreshTokenCookie.setPath("/"); // 동일한 경로
            deleteRefreshTokenCookie.setMaxAge(0); // 쿠키 삭제 설정

            response.addCookie(deleteRefreshTokenCookie); // 삭제할 쿠키를 response에 추가
        }

        JwtTokenDto jwtTokenResponse = userService.login(request);

        Cookie refreshTokenCookie = new Cookie(
                "refreshToken",
                jwtTokenResponse.getRefreshToken()
        );

//        refreshTokenCookie.setHttpOnly(true); // 자바스크립트에서 접근할 수 없도록 설정
//        refreshTokenCookie.setSecure(true); // HTTPS에서만 전송되도록 설정 (생산 환경에서 사용)
        refreshTokenCookie.setPath("/"); // 쿠키의 유효 경로 설정
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 쿠키의 유효 기간 설정 (예: 7일)

        response.addCookie(refreshTokenCookie);
        //쿠키의 정보를 response 데이터에 넣는다.

        return ResponseEntity.ok().body(JwtTokenResponse
                .builder()
                .accessToken(jwtTokenResponse.getAccessToken())
                .refreshToken(jwtTokenResponse.getRefreshToken())
                .isAdmin(jwtTokenResponse.getRole().equals(UserRole.ADMIN))
                .build());
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<RefreshTokenResponse> refreshAccessToken(@RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // refreshToken 검증 및 새로운 accessToken 생성
        String newAccessToken = tokenService.createNewAccessToken(refreshToken);

        if (newAccessToken == null) {
            return ResponseEntity.status(401).build(); // 토큰이 유효하지 않은 경우 401 Unauthorized 응답
        }

        return ResponseEntity.ok(new RefreshTokenResponse(newAccessToken));
    }

    @GetMapping("/api/protected")
    public ResponseEntity<String> getProtectedResource(@CookieValue("accessToken") String accessToken) {
        if (userService.validateToken(accessToken)) {
            return ResponseEntity.ok("Protected data");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }
    }

}
