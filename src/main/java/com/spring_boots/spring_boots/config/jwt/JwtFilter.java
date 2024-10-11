package com.spring_boots.spring_boots.config.jwt;

import com.spring_boots.spring_boots.config.jwt.impl.JwtProviderImpl;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@RequiredArgsConstructor
@Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProviderImpl tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // JWT 토큰을 쿠키에서 추출
        String jwtAccessToken = resolveAccessTokenFromCookies(request);
        String jwtRefreshToken = resolveRefreshTokenFromCookies(request);

        if (jwtAccessToken != null && tokenProvider.validateToken(jwtAccessToken)) {
            // 액세스토큰이 유효한 경우, Authentication 객체 생성
            Authentication authentication = tokenProvider.getAuthentication(jwtAccessToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else if (jwtRefreshToken != null) {
            // 액세스토큰이 만료된 경우, 리프레시토큰 검증
            log.info("Access token expired, validating refresh token...");

            // 리프레시토큰이 유효하다면 새로운 액세스토큰 발급
            // 해당 메소드에서 유효성 검사까지 같이 진행
            String newAccessToken = tokenProvider.generateAccessTokenFromRefreshToken(jwtRefreshToken);

            log.info("리프레시토큰 발급 완료..!");

            // 새로운 액세스토큰을 쿠키에 저장
            Cookie newAccessTokenCookie = new Cookie("accessToken", newAccessToken);
//            newAccessTokenCookie.setHttpOnly(true);
//            newAccessTokenCookie.setSecure(true);
            newAccessTokenCookie.setPath("/");
            response.addCookie(newAccessTokenCookie);

            // 새로운 액세스토큰으로 Authentication 객체 생성
            Authentication authentication = tokenProvider.getAuthentication(newAccessToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    // accessToken을 쿠키에서 추출
    private String resolveAccessTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        // 쿠키에서 "accessToken" 추출
        Optional<Cookie> jwtCookie = Arrays.stream(request.getCookies())
                .filter(cookie -> "accessToken".equals(cookie.getName()))
                .findFirst();

        return jwtCookie.map(Cookie::getValue).orElse(null);
    }

    // refreshToken을 쿠키에서 추출
    private String resolveRefreshTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        // 쿠키에서 "refreshToken" 추출
        Optional<Cookie> jwtCookie = Arrays.stream(request.getCookies())
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .findFirst();

        return jwtCookie.map(Cookie::getValue).orElse(null);
    }

    //실제 토큰 발급
//    private Optional<String> resolveToken(HttpServletRequest request) {
//        String bearerToken = request.getHeader(AUTHORIZATION_TOKEN_KEY);
//        //요청된 데이터에서 Authorization 에 해당하는 데이터를 가지고 온다.
//        if (bearerToken!=null && bearerToken.startsWith(TOKEN_PREFIX)) {
//            return Optional.of(bearerToken.substring(TOKEN_PREFIX.length()));
//            //만약 값이 있다면 "bearer "를 제외한 값을 return
//        }
//
//        return Optional.empty();    //없다면 null
//    }
}
