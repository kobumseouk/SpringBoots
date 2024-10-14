package com.spring_boots.spring_boots.user.service;

import com.spring_boots.spring_boots.config.jwt.impl.AuthTokenImpl;
import com.spring_boots.spring_boots.config.jwt.impl.JwtProviderImpl;
import com.spring_boots.spring_boots.user.domain.UserRole;
import com.spring_boots.spring_boots.user.domain.Users;
import com.spring_boots.spring_boots.user.domain.UsersInfo;
import com.spring_boots.spring_boots.user.dto.request.*;
import com.spring_boots.spring_boots.user.dto.response.UserResponseDto;
import com.spring_boots.spring_boots.user.repository.UserInfoRepository;
import com.spring_boots.spring_boots.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtProviderImpl jwtProvider;
    private final UserInfoRepository userInfoRepository;

    public Users save(UserSignupRequestDto dto) {
        if (userRepository.existsByUserRealId(dto.getUserRealId())) {
            throw new IllegalArgumentException("이미 존재하는 ID 입니다.");
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        Users user = Users.builder()
                .username(dto.getUsername())
                .userRealId(dto.getUserRealId())
                .email(dto.getEmail())
                .password(encoder.encode(dto.getPassword()))
                .role(UserRole.USER)
                .build();
        Users saveUser = userRepository.save(user);

        return saveUser;
    }

    public Users findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Unexpected user"));
    }

    public Users findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Unexpected user"));
    }

    public JwtTokenDto login(JwtTokenLoginRequest request) {
        Users user = userRepository.findByUserRealId(request.getUserRealId())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 실제 ID 입니다."));

        if (user.isDeleted()) {
            throw new IllegalArgumentException("정보가 삭제된 회원입니다.");
        }

        if (!bCryptPasswordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
        }

        Map<String, Object> claims = Map.of(
                "accountId", user.getUserId(),  //JWT 클래임에 accountId
                "role", user.getRole(),  //JWT 클래임에 role
                "userRealId", user.getUserRealId()   //JWT 클래임에 실제 ID 추가
        );

        AuthTokenImpl accessToken = jwtProvider.createAccessToken(
                user.getUserRealId(),   //토큰에 실제 ID 정보 입력
                user.getRole(),
                claims
        );

        AuthTokenImpl refreshToken = jwtProvider.createRefreshToken(
                user.getUserRealId(),   //토큰에 실제 ID 정보 입력
                user.getRole(),
                claims
        );

        return JwtTokenDto.builder()
                .accessToken(accessToken.getToken())
                .refreshToken(refreshToken.getToken())
                .role(user.getRole())
                .build();
    }

    public List<UserResponseDto> findAll() {
        List<Users> users = userRepository.findAll();

        return users.stream()
                .map(Users::toResponseDto)  // Users 객체를 UserResponseDto로 변환
                .collect(Collectors.toList());
    }

    @Transactional
    public void update(Users user, UserUpdateRequestDto userUpdateRequestDto, Long userInfoId) {
        if (!bCryptPasswordEncoder.matches(userUpdateRequestDto.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
        }

        UsersInfo usersInfo = userInfoRepository.findById(userInfoId).orElse(null);
        if (usersInfo != null) {
            usersInfo.updateUserInfo(userUpdateRequestDto);
        }

        user.updateUser(userUpdateRequestDto);
    }

    @Transactional
    public void deleteUser(Users authUser) {
        userRepository.delete(authUser);
    }

    public boolean isDeleteUser(Users authUser) {
        Optional<Users> findUser = userRepository.findById(authUser.getUserId());
        if (findUser.isPresent()) {
            return false;   //존재하면 false 반환
        }

        return true;
    }

    @Transactional
    public void softDeleteUser(Users authUser) {
        authUser.deleteUser();  //소프트 딜리트
    }

    public boolean checkPassword(Users authUser, UserPasswordRequestDto request) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (encoder.matches(request.getPassword(), authUser.getPassword())) {
            return true;    //비밀번호가 맞으면 true
        }

        return false;   //맞지않으면 false
    }

    public boolean isDuplicateUserRealId(String userRealId) {
        return userRepository.existsByUserRealId(userRealId);
    }

    @Transactional
    public boolean grantAdminToken(Users authUser, AdminGrantTokenRequestDto adminGrantTokenRequestDto) {
        //임의 토큰 만들기
        String tempToken = bCryptPasswordEncoder.encode("admin");
        String adminToken = adminGrantTokenRequestDto.getAdminToken();
        if (bCryptPasswordEncoder.matches(adminToken, tempToken)) {
            authUser.updateToAdminRole();
            return true;
        } else {
            log.info("잘못된 관리자 토큰");
            return false;
        }

    }

    public boolean isGrantAdmin(Users authUser) {
        return authUser.getRole().equals(UserRole.ADMIN);
    }

    public boolean validateToken(String accessToken) {
        return jwtProvider.validateToken(accessToken);
    }

    public boolean validateAdminToken(String accessToken) {
        return jwtProvider.validateAdminToken(accessToken);
    }
}
