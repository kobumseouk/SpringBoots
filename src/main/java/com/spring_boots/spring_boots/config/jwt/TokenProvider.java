package com.spring_boots.spring_boots.config.jwt;

import com.spring_boots.spring_boots.user.domain.Users;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class TokenProvider {

    private final JwtProperties jwtProperties;
    //issuer : test@example.com
    //secret-key : spring-boots

    //만료시간 + 토큰생성
    public String generateToken(Users user, Duration expiredAt) {
        Date now = new Date();
        return makeToken(new Date(now.getTime() + expiredAt.toMillis()), user); //토큰생성
    }

    //토큰 생성
    private String makeToken(Date expiry, Users user) {
        Date now = new Date();  //현재 시간

        return Jwts.builder()
                //헤더
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)    //typ,JWT -> 헤더 타입 설정
                //페이로드
                .setIssuer(jwtProperties.getIssuer())   //토큰 발급자 정보
                .setIssuedAt(now) //토큰 발급시간 설정
                .setExpiration(expiry)  //토큰 만료시간 설정
                .setSubject(user.getUserRealId())   //토큰의 주체를 사용자의 실제 ID로 설정
                .claim("id", user.getUserId())  // 사용자 ID를 클레임에 추가
                //시그니처
                .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecretKey())
                //HMAC SHA-256 알고리즘을 서버 비밀키로 서명
                .compact(); //최종적으로 JWT 토큰 생성하여 반환
    }


    //토큰 검증
    public boolean validToken(String token) {
        try {
            // 요청된 토큰을 파싱하여 유효한지 검증
            Jwts.parser()
                    .setSigningKey(jwtProperties.getSecretKey())    //비밀키를 사용하여 서명 검증
                    .parseClaimsJws(token);

            return true;    //유효할 경우 true 반환
        } catch (Exception e) {
            return false;   //유효하지 않을 경우 false 반환
        }
    }

    //Spring Security 인증 객체 생성
    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);   //토큰에서 클레임을 가져온다.

        // 사용자 권한 설정
        Set<SimpleGrantedAuthority> authorities
                = Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));

        //Spring Security 에서 제공하는 User 인증정보를 사용 하여 Authentication 객체 생성
        return new UsernamePasswordAuthenticationToken(
                new org.springframework.security.core.userdetails.User(
                        claims.getSubject(), "", authorities), token, authorities
        );
    }

    //토큰에서 클레임만 파싱
    private Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(jwtProperties.getSecretKey())        //비밀키 사용해 토큰 파싱
                .parseClaimsJws(token)      //토큰에서 JWT의 본문 가져옴
                .getBody(); //클레임 반환
    }

}
