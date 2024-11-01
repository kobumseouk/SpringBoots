package com.spring_boots.spring_boots.user.service;

import com.spring_boots.spring_boots.config.jwt.impl.JwtProviderImpl;
import com.spring_boots.spring_boots.user.domain.UserRole;
import com.spring_boots.spring_boots.user.domain.Users;
import com.spring_boots.spring_boots.user.dto.request.AdminCodeRequestDto;
import com.spring_boots.spring_boots.user.dto.request.AdminGrantTokenRequestDto;
import com.spring_boots.spring_boots.user.dto.response.UserAdminCountResponseDto;
import com.spring_boots.spring_boots.user.dto.response.UserResponseDto;
import com.spring_boots.spring_boots.user.repository.UserInfoRepository;
import com.spring_boots.spring_boots.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtProviderImpl jwtProvider;
    private final UserInfoRepository userInfoRepository;

    @Value("${admin.code}")
    private String adminCode;

    //관리자코드체크
    public boolean checkAdminCode(AdminCodeRequestDto adminCodeDto) {
        //임의 토큰 만들기
        String tempAdminCode = bCryptPasswordEncoder.encode(adminCode);
        String adminCode = adminCodeDto.getAdminCode();
        if (bCryptPasswordEncoder.matches(adminCode, tempAdminCode)) {
            return true;
        } else {
            log.info("잘못된 관리자 토큰");
            return false;
        }
    }

    public Page<UserResponseDto> getUsersByCreatedAt(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Users> usersPage = userRepository.findAll(pageable);

        return usersPage.map(Users::toResponseDto);
    }


    public UserAdminCountResponseDto countUsers() {
        List<Users> users = userRepository.findAll();
        long countAdmin = users.stream()
                .filter(user -> user.getRole().equals(UserRole.ADMIN))
                .count();
        long totalUsers = users.stream()
                .filter(user -> !user.isDeleted())
                .count();
        return UserAdminCountResponseDto.builder()
                .countAdmin(countAdmin)
                .totalUser(totalUsers)
                .build();
    }

    @Transactional
    public void grantRole(Users authUser, AdminGrantTokenRequestDto adminGrantTokenRequestDto) {
        authUser.updateToRole(adminGrantTokenRequestDto);
    }

    public boolean validateAdminToken(String accessToken) {
        return jwtProvider.validateAdminToken(accessToken);
    }
}
