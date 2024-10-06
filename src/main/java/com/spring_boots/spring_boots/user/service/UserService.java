package com.spring_boots.spring_boots.user.service;

import com.spring_boots.spring_boots.config.jwt.impl.AuthTokenImpl;
import com.spring_boots.spring_boots.config.jwt.impl.JwtProviderImpl;
import com.spring_boots.spring_boots.user.domain.UserRole;
import com.spring_boots.spring_boots.user.domain.Users;
import com.spring_boots.spring_boots.user.dto.request.JwtTokenDto;
import com.spring_boots.spring_boots.user.dto.request.JwtTokenLoginRequest;
import com.spring_boots.spring_boots.user.dto.request.UserSignupRequestDto;
import com.spring_boots.spring_boots.user.dto.request.UserUpdateRequestDto;
import com.spring_boots.spring_boots.user.dto.response.UserResponseDto;
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
        log.info("요청 비밀번호 : {}, 실제 비밀번호 : {}", request.getPassword(), user.getPassword());

        if (!bCryptPasswordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
        }

        Map<String, Object> claims = Map.of(
                "accountId", user.getUserId(),  //JWT 클래임에 accountId
                "role", user.getRole(),  //JWT 클래임에 role
                "userRealId",user.getUserRealId()   //JWT 클래임에 실제 ID 추가
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
                .build();
    }

    public List<UserResponseDto> findAll() {
        List<Users> users = userRepository.findAll();

        return users.stream()
                .map(Users::toResponseDto)  // Users 객체를 UserResponseDto로 변환
                .collect(Collectors.toList());
    }

    @Transactional
    public void update(Users user, UserUpdateRequestDto userUpdateRequestDto) {
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
}
