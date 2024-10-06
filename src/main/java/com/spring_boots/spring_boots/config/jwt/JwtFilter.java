package com.spring_boots.spring_boots.config.jwt;

import com.spring_boots.spring_boots.config.jwt.impl.AuthTokenImpl;
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

import static com.spring_boots.spring_boots.config.jwt.UserConstants.AUTHORIZATION_TOKEN_KEY;
import static com.spring_boots.spring_boots.config.jwt.UserConstants.TOKEN_PREFIX;

@RequiredArgsConstructor
@Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    //        private final TokenProvider tokenProvider;
    private final JwtProviderImpl tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // JWT 토큰을 쿠키에서 추출
        String jwtToken = resolveTokenFromCookies(request);

        if (jwtToken != null && tokenProvider.validateToken(jwtToken)) {
            // 토큰이 유효한 경우, Authentication 객체 생성
            Authentication authentication = tokenProvider.getAuthentication(jwtToken);
            // SecurityContextHolder에 인증 정보 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        // 쿠키에서 "accessToken" 또는 "refreshToken" 추출
        Optional<Cookie> jwtCookie = Arrays.stream(request.getCookies())
                .filter(cookie -> "accessToken".equals(cookie.getName()) ||
                        "refreshToken".equals(cookie.getName()))
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
