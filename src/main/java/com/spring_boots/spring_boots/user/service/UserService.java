package com.spring_boots.spring_boots.user.service;

import com.spring_boots.spring_boots.config.jwt.impl.AuthTokenImpl;
import com.spring_boots.spring_boots.config.jwt.impl.JwtProviderImpl;
import com.spring_boots.spring_boots.user.domain.Provider;
import com.spring_boots.spring_boots.user.domain.UserRole;
import com.spring_boots.spring_boots.user.domain.Users;
import com.spring_boots.spring_boots.user.domain.UsersInfo;
import com.spring_boots.spring_boots.user.dto.UserDto;
import com.spring_boots.spring_boots.user.dto.request.*;
import com.spring_boots.spring_boots.user.dto.response.UserResponseDto;
import com.spring_boots.spring_boots.user.exception.PasswordNotMatchException;
import com.spring_boots.spring_boots.user.exception.UserDeletedException;
import com.spring_boots.spring_boots.user.exception.UserNotFoundException;
import com.spring_boots.spring_boots.user.repository.UserInfoRepository;
import com.spring_boots.spring_boots.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
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

        Users user = Users.builder()
                .username(dto.getUsername())
                .userRealId(dto.getUserRealId())
                .email(dto.getEmail())
                .password(bCryptPasswordEncoder.encode(dto.getPassword()))
                .role(UserRole.USER)
                .build();

        return userRepository.save(user);
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
                .orElseThrow(() -> new UserNotFoundException("가입되지 않은 ID 입니다."));

        if (user.isDeleted()) {
            throw new UserDeletedException("회원 정보가 삭제된 상태입니다.");
        }

        if (!bCryptPasswordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new PasswordNotMatchException("잘못된 비밀번호입니다.");
        }

        Map<String, Object> claims = Map.of(
                "accountId", user.getUserId(),  //JWT 클래임에 accountId
                "role", user.getRole(),  //JWT 클래임에 role
                "provider",user.getProvider(),
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
    public void updateNoneUser(Users user, UserUpdateRequestDto userUpdateRequestDto, Long userInfoId) {
        if (!bCryptPasswordEncoder.matches(userUpdateRequestDto.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
        }

        UsersInfo usersInfo = userInfoRepository.findById(userInfoId).orElse(null);
        //회원정보가 이미 있다면 업데이트, 그렇지않다면 생성
        if (usersInfo != null) {
            usersInfo.updateUserInfo(userUpdateRequestDto);
        } else {
            UsersInfo newUsersInfo = userUpdateRequestDto.toUsersInfo(user);
            userInfoRepository.save(newUsersInfo);
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
    public void grantRole(Users authUser, AdminGrantTokenRequestDto adminGrantTokenRequestDto) {
        authUser.updateToRole(adminGrantTokenRequestDto);
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

    //관리자코드체크
    public boolean checkAdminCode(AdminCodeRequestDto adminCodeDto) {
        //임의 토큰 만들기
        String tempAdminCode = bCryptPasswordEncoder.encode("admin");
        String adminCode = adminCodeDto.getAdminCode();
        if (bCryptPasswordEncoder.matches(adminCode, tempAdminCode)) {
            return true;
        } else {
            log.info("잘못된 관리자 토큰");
            return false;
        }
    }

    //엔티티 변경
    public Users getUserEntityByDto(UserDto userDto) {
        return userRepository.findById(userDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다: " + userDto.getUserId()));
    }

    //oauth2 의 경우 이메일과 아이디 동일
    public Users findByUserRealId(String email) {
        return userRepository.findByUserRealId(email)
                .orElseThrow(() -> new IllegalArgumentException("회원정보가 존재하지않습니다."));
    }

    @Transactional
    public void updateGoogleUser(Users user, UserUpdateRequestDto userUpdateRequestDto, Long userInfoId) {
        UsersInfo usersInfo = userInfoRepository.findById(userInfoId).orElse(null);
        //회원정보가 이미 있다면 업데이트, 그렇지않다면 생성
        if (usersInfo != null) {
            usersInfo.updateUserInfo(userUpdateRequestDto);
        } else {
            UsersInfo newUsersInfo = userUpdateRequestDto.toUsersInfo(user);
            userInfoRepository.save(newUsersInfo);
        }
    }
}
