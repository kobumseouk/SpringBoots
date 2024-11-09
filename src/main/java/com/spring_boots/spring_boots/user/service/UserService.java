package com.spring_boots.spring_boots.user.service;

import com.spring_boots.spring_boots.config.jwt.impl.AuthTokenImpl;
import com.spring_boots.spring_boots.config.jwt.impl.JwtProviderImpl;
import com.spring_boots.spring_boots.user.domain.*;
import com.spring_boots.spring_boots.user.dto.UserDto;
import com.spring_boots.spring_boots.user.dto.request.*;
import com.spring_boots.spring_boots.user.dto.response.UserAdminCountResponseDto;
import com.spring_boots.spring_boots.user.dto.response.UserDeleteResponseDto;
import com.spring_boots.spring_boots.user.dto.response.UserResponseDto;
import com.spring_boots.spring_boots.user.exception.PasswordNotMatchException;
import com.spring_boots.spring_boots.user.exception.TokenNotFoundException;
import com.spring_boots.spring_boots.user.exception.UserDeletedException;
import com.spring_boots.spring_boots.user.exception.UserNotFoundException;
import com.spring_boots.spring_boots.user.repository.TokenRedisRepository;
import com.spring_boots.spring_boots.user.repository.UserInfoRepository;
import com.spring_boots.spring_boots.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    private final UserInfoRepository userInfoRepository;
    private final TokenRedisRepository tokenRedisRepository;

    //일반 회원가입
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
                .provider(Provider.NONE)
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

    public List<UserResponseDto> findAll() {
        List<Users> users = userRepository.findAll();

        return users.stream()
                .map(Users::toResponseDto)  // Users 객체를 UserResponseDto로 변환
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateNoneUser(UserDto userDto, UserUpdateRequestDto userUpdateRequestDto, Long userInfoId) {
        if (!bCryptPasswordEncoder.matches(userUpdateRequestDto.getCurrentPassword(), userDto.getPassword())) {
            throw new PasswordNotMatchException("잘못된 비밀번호입니다.");
        }
        Users user = findById(userDto.getUserId());

        //Users 엔티티에 있는 userId 값을 찾아서 반환
        UsersInfo usersInfo = userInfoRepository.findByUsers_UserId(userInfoId).orElse(null);
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
    public UserDeleteResponseDto softDeleteUser(UserDto userDto) {
        Users user = findById(userDto.getUserId());
        return user.deleteUser();
    }

    public boolean checkPassword(UserDto authUser, UserPasswordRequestDto request) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (encoder.matches(request.getPassword(), authUser.getPassword())) {
            return true;    //비밀번호가 맞으면 true
        }

        return false;   //맞지않으면 false
    }

    public boolean isDuplicateUserRealId(String userRealId) {
        return userRepository.existsByUserRealId(userRealId);
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
    public void updateGoogleUser(UserDto userDto, UserUpdateRequestDto userUpdateRequestDto, Long userInfoId) {
        UsersInfo usersInfo = userInfoRepository.findByUsers_UserId(userInfoId).orElse(null);
        Users user = findById(userDto.getUserId());

        //회원정보가 이미 있다면 업데이트, 그렇지않다면 생성
        if (usersInfo != null) {
            usersInfo.updateUserInfo(userUpdateRequestDto);
        } else {
            UsersInfo newUsersInfo = userUpdateRequestDto.toUsersInfo(user);
            userInfoRepository.save(newUsersInfo);
        }
    }


    public boolean validateSignup(UserSignupRequestDto userSignupRequestDto) {
        // username 유효성 검증: 2~20글자, 숫자 포함 불가
        String username = userSignupRequestDto.getUsername();
        boolean isUsernameValid = username != null && username.length() >= 2 && username.length() <= 20 && !username.matches(".*\\d.*");
        if (!isUsernameValid) {
            return false; // 유효성 검증 실패
        }

        // userRealId 유효성 검증: 6~20글자
        String userRealId = userSignupRequestDto.getUserRealId();
        boolean isUserRealIdValid = userRealId != null && userRealId.length() >= 6 && userRealId.length() <= 20;
        if (!isUserRealIdValid) {
            return false; // 유효성 검증 실패
        }

        // email 유효성 검증: 이메일 형식
        String email = userSignupRequestDto.getEmail();
        boolean isEmailValid = email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
        if (!isEmailValid) {
            return false; // 유효성 검증 실패
        }

        // password 유효성 검증: 8~20글자, 영문자, 특수문자 포함
        String password = userSignupRequestDto.getPassword();
        boolean isPasswordValid = password != null && password.length() >= 8 && password.length() <= 20
                && password.matches("^(?=.*[a-zA-Z])(?=.*\\W).+$");
        if (!isPasswordValid) {
            return false; // 유효성 검증 실패
        }

        return true; // 모든 유효성 검증 통과

    }

    public boolean validateLogin(JwtTokenLoginRequest request) {
        //아이디 값이 빈값이면 false
        String userRealId = request.getUserRealId();
        if (userRealId.isEmpty()) {
            return false;
        }

        //패스워드 값이 빈값이면 false
        String password = request.getPassword();
        if (password.isEmpty()) {
            return false;
        }

        return true;
    }

    public boolean validateUpdateUser(UserUpdateRequestDto request) {
        //변경할 password 유효성 검증: 8~20글자, 영문자, 특수문자 포함
        String password = request.getUpdatePassword();
        boolean isPasswordValid = password != null && password.length() >= 8 && password.length() <= 20
                && password.matches("^(?=.*[a-zA-Z])(?=.*\\W).+$");
        //패스워드 값이 없으면 true, 있으면 유효성 검증
        if (password!=null && !isPasswordValid) {
            return false; // 유효성 검증 실패
        }

        // email 유효성 검증: 이메일 형식
        String email = request.getEmail();
        boolean isEmailValid = email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
        if (!isEmailValid) {
            return false; // 유효성 검증 실패
        }

        return true;
    }

//    @Transactional    // redis는 트랜잭션이 엄격하게 필요하지않음..?=
    public void deleteRefreshTokenInRedis(UserDto user) {
        //토큰삭제
        tokenRedisRepository.deleteById(user.getUserRealId());
    }
}
