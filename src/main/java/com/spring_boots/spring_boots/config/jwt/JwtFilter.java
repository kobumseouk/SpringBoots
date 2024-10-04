package com.spring_boots.spring_boots.config.jwt;

import com.spring_boots.spring_boots.config.jwt.impl.AuthTokenImpl;
import com.spring_boots.spring_boots.config.jwt.impl.JwtProviderImpl;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

import static com.spring_boots.spring_boots.config.UserConstants.AUTHORIZATION_TOKEN_KEY;
import static com.spring_boots.spring_boots.config.UserConstants.TOKEN_PREFIX;

@RequiredArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {

    //    private final TokenProvider tokenProvider;
    private final JwtProviderImpl tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Optional<String> token = resolveToken(request);   //토큰 발급

        //토큰 유효성 검증
        if (token.isPresent()) {
            AuthTokenImpl jwtToken =
                    tokenProvider.convertAuthToken(token.get().split(" ")[1]);

            if (jwtToken.validate()) {
                Authentication authentication =
                        tokenProvider.getAuthentication(jwtToken);

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);
            }
        }
//        if (tokenProvider.validToken(token)) {
//            Authentication authentication= tokenProvider.getAuthentication(token);  //인증객체 갖고오기
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//            //SecurityContextHolder 에 인증정보 저장
//        }

        filterChain.doFilter(request, response);
    }

    //실제 토큰 발급
    private Optional<String> resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_TOKEN_KEY);
        //요청된 데이터에서 Authorization 에 해당하는 데이터를 가지고 온다.
        if (bearerToken!=null && bearerToken.startsWith(TOKEN_PREFIX)) {
            return Optional.of(bearerToken.substring(TOKEN_PREFIX.length()));
            //만약 값이 있다면 "bearer "를 제외한 값을 return
        }

        return Optional.empty();    //없다면 null
    }
}
