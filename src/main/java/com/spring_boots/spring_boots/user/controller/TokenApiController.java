package com.spring_boots.spring_boots.user.controller;

import com.spring_boots.spring_boots.common.util.CookieUtil;
import com.spring_boots.spring_boots.user.domain.UserRole;
import com.spring_boots.spring_boots.user.dto.request.JwtTokenDto;
import com.spring_boots.spring_boots.user.dto.request.JwtTokenLoginRequest;
import com.spring_boots.spring_boots.user.dto.request.RefreshTokenRequest;
import com.spring_boots.spring_boots.user.dto.response.JwtTokenResponse;
import com.spring_boots.spring_boots.user.dto.response.RefreshTokenResponse;
import com.spring_boots.spring_boots.user.dto.response.UserValidateTokenResponseDto;
import com.spring_boots.spring_boots.user.exception.PasswordNotMatchException;
import com.spring_boots.spring_boots.user.exception.UserDeletedException;
import com.spring_boots.spring_boots.user.exception.UserNotFoundException;
import com.spring_boots.spring_boots.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.spring_boots.spring_boots.config.jwt.UserConstants.*;

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
            @CookieValue(value = "accessToken", required = false) Cookie existingAccessTokenCookie
    ) {
        if (!userService.validateLogin(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(JwtTokenResponse.builder()
                            .message("아이디와 비밀번호를 입력하세요").build());
        }

        // 기존 쿠키 삭제 로직
        if (existingAccessTokenCookie != null) {
            CookieUtil.deleteTokenCookie(response,ACCESS_TOKEN_TYPE_VALUE);
        }


        try {
            JwtTokenDto jwtTokenResponse = userService.login(request);

            CookieUtil.addCookie(response, ACCESS_TOKEN_TYPE_VALUE,
                    jwtTokenResponse.getAccessToken(),
                    (int) ACCESS_TOKEN_DURATION.toSeconds());

            return ResponseEntity.ok().body(JwtTokenResponse
                    .builder()
                    .accessToken(jwtTokenResponse.getAccessToken())
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

    //토큰 유효성 api
    @GetMapping("/protected")
    public ResponseEntity<UserValidateTokenResponseDto> getProtectedResource(@CookieValue(
            value = "accessToken", required = false, defaultValue = "") String accessToken) {
        if (accessToken == null || accessToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(UserValidateTokenResponseDto.builder()
                            .message("not login").build());
        }
        if (userService.validateToken(accessToken)) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(UserValidateTokenResponseDto.builder()
                            .message("success").build());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(UserValidateTokenResponseDto.builder()
                            .message("fail").build());
        }
    }
}
