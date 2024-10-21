package com.spring_boots.spring_boots.user.service;

import com.spring_boots.spring_boots.user.domain.Provider;
import com.spring_boots.spring_boots.user.domain.UserRole;
import com.spring_boots.spring_boots.user.domain.Users;
import com.spring_boots.spring_boots.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@RequiredArgsConstructor
@Service
public class OAuth2UserCustomService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User user = super.loadUser(userRequest); // 요청을 바탕으로 유저 정보를 담은 객체 반환
        saveOrUpdate(user);

        return user;
    }

    // 유저가 있으면 업데이트, 없으면 유저 생성
    private Users saveOrUpdate(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = (String) attributes.get("email");
        String username = (String) attributes.get("name");


        Users user = userRepository.findByUserRealId(email)
                .map(entity -> entity.updateName(username)) //유저가 있으면 새로운 이름으로변경
                .orElse(Users.builder() //없다면 새로운 계정 생성
                        .userRealId(email) //구글의 경우 아이디와 이메일을 동일
                        .email(email)
                        .username(username)
                        .provider(Provider.GOOGLE)
                        .role(UserRole.USER)
                        .isDeleted(false)
                        .build());

        return userRepository.save(user);
    }
}

