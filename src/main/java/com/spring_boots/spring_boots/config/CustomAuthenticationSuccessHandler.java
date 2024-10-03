package com.spring_boots.spring_boots.config;

import com.spring_boots.spring_boots.config.jwt.TokenProvider;
import com.spring_boots.spring_boots.orders.entity.RefreshToken;
import com.spring_boots.spring_boots.user.domain.Users;
import com.spring_boots.spring_boots.user.repository.RefreshTokenRepository;
import com.spring_boots.spring_boots.user.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Duration;

import static com.spring_boots.spring_boots.config.UserConstants.*;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {


    private final TokenProvider tokenProvider;
//    private final UserService userService;    //순환참조문제
    private final RefreshTokenRepository refreshTokenRepository;
//    private final OAuth2AuthorizationRequestBasedOnCookieRepository authorizationRequestRepository; // OAuth2 인증 요청 쿠키 저장소

    //로그인 성공시 호출 되는 메서드
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        Users user = (Users) authentication.getPrincipal();

        // 리프레시 토큰 생성 및 저장
        String refreshToken = tokenProvider.generateToken(user, REFRESH_TOKEN_DURATION); // 리프레시 토큰 생성
        saveRefreshToken(user.getUserId(), refreshToken); // 리프레시 토큰 저장
//        addRefreshTokenToCookie(request, response, refreshToken); // 리프레시 토큰을 쿠키에 추가

        // 액세스 토큰 생성
        String accessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION); // 액세스 토큰 생성
        String targetUrl = getTargetUrl(accessToken); // 리다이렉트 URL 생성

        // 인증 관련 쿠키와 정보를 정리하고 리다이렉트 처리
//        clearAuthenticationAttributes(request, response); // 인증 속성 정리
        getRedirectStrategy().sendRedirect(request, response, targetUrl); // 지정된 URL로 리다이렉트

    }

    // 인증 관련 속성을 정리하는 메서드
//    private void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
//        super.clearAuthenticationAttributes(request); // 부모 클래스 메서드 호출하여 속성 정리
//        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response); // 인증 요청 쿠키 삭제
//    }

//    private void addRefreshTokenToCookie(HttpServletRequest request, HttpServletResponse response, String refreshToken) {
//        int cookieMaxAge = (int) REFRESH_TOKEN_DURATION.toSeconds(); // 쿠키 유효 기간 설정
//
//        CookieUtil.deleteCookie(request, response, REFRESH_TOKEN_TYPE_VALUE, true, true); // 기존 쿠키 삭제
//        CookieUtil.addCookie(response, REFRESH_TOKEN_TYPE_VALUE, refreshToken, cookieMaxAge, true, true); // 새 쿠키 추가
//    }

    // 리다이렉트할 URL을 생성하는 메서드
    private String getTargetUrl(String token) {
        return UriComponentsBuilder.fromUriString("/articles") // 리다이렉트 경로 설정
                .queryParam("token", token) // URL에 액세스 토큰을 쿼리 파라미터로 추가
                .build()
                .toUriString();
    }

    private void saveRefreshToken(Long userId, String newRefreshToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByUserId(userId)
                .map(entity -> entity.update(newRefreshToken)) // 기존 토큰이 있으면 업데이트
                .orElse(new RefreshToken(userId, newRefreshToken)); // 없으면 새로 생성

        refreshTokenRepository.save(refreshToken); // 저장소에 리프레시 토큰 저장
    }

}
