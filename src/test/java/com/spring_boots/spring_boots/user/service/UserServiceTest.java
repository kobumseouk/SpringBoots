package com.spring_boots.spring_boots.user.service;

import com.spring_boots.spring_boots.config.jwt.impl.AuthTokenImpl;
import com.spring_boots.spring_boots.config.jwt.impl.JwtProviderImpl;
import com.spring_boots.spring_boots.user.domain.Provider;
import com.spring_boots.spring_boots.user.domain.UserRole;
import com.spring_boots.spring_boots.user.domain.Users;
import com.spring_boots.spring_boots.user.domain.UsersInfo;
import com.spring_boots.spring_boots.user.dto.request.*;
import com.spring_boots.spring_boots.user.dto.response.UserResponseDto;
import com.spring_boots.spring_boots.user.exception.PasswordNotMatchException;
import com.spring_boots.spring_boots.user.repository.UserRepository;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ActiveProfiles("test")
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtProviderImpl jwtProvider;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("회원가입 성공테스트")
    public void testSaveSuccess() {
        UserSignupRequestDto request = new UserSignupRequestDto("testUser", "testRealId", "test@example.com", "password");

        Users user = Users.builder()
                .username(request.getUsername())
                .userRealId(request.getUserRealId())
                .email(request.getEmail())
                .password("encodedPassword")
                .role(UserRole.USER)
                .build();

        when(userRepository.existsByUserRealId(request.getUserRealId())).thenReturn(false);
        when(bCryptPasswordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(Users.class))).thenReturn(user);

        Users savedUser = userService.save(request);

        assertNotNull(savedUser);
        assertEquals("encodedPassword", savedUser.getPassword());
        assertEquals("testRealId", savedUser.getUserRealId());
        assertEquals("testUser", savedUser.getMember());
        assertEquals("test@example.com", savedUser.getEmail());
        verify(userRepository, times(1)).save(any(Users.class));
    }

    @Test
    @DisplayName("동일한 아이디 중복 예외발생")
    public void testSaveFail() {
        UserSignupRequestDto request = new UserSignupRequestDto("testUser", "testRealId", "test@example.com", "password");

        when(userRepository.existsByUserRealId(request.getUserRealId())).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.save(request);
        });

        assertEquals("이미 존재하는 ID 입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("로그인 성공시")
    public void testLoginSuccess() {
        JwtTokenLoginRequest request = new JwtTokenLoginRequest("testRealId", "password");

        Users user = Users.builder()
                .userId(1000L)  //임의 설정
                .userRealId(request.getUserRealId())
                .password("encodedPassword")
                .role(UserRole.USER)
                .provider(Provider.NONE)    //일반회원가입
                .build();

        when(userRepository.findByUserRealId(request.getUserRealId())).thenReturn(Optional.of(user));
        when(bCryptPasswordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);

        Map<String, Object> claims = Map.of(
                "accountId", user.getUserId(),  //JWT 클래임에 accountId
                "role", user.getRole(),  //JWT 클래임에 role
                "provider",user.getProvider(),
                "userRealId", user.getUserRealId()   //JWT 클래임에 실제 ID 추가
        );

        /* powershell 에서 Base64 HS512 자동생성
        * # 64 바이트의 랜덤 키 생성
            $randBytes = New-Object byte[] 64
            (New-Object Security.Cryptography.RNGCryptoServiceProvider).GetBytes($randBytes)
            $base64Key = [Convert]::ToBase64String($randBytes)

            # 출력
            $base64Key

        * */
        String secretKey = "rY5KolGhGmsYAKDJ8Hz1I8C6M8eMQz1b3D7HthUdAQY8ta9+gm8iUbB27OGWJya8TZ16r5CEWm/0dHO3xNih4A==";
        //프롬프트 암호화자동생성

        Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        AuthTokenImpl accessToken = new AuthTokenImpl("accessToken", key);
        AuthTokenImpl refreshToken = new AuthTokenImpl("refreshToken", key);

        when(jwtProvider.createAccessToken(user.getUserRealId(), user.getRole(), claims)).thenReturn(accessToken);
        when(jwtProvider.createRefreshToken(user.getUserRealId(), user.getRole(), claims)).thenReturn(refreshToken);

        JwtTokenDto jwtTokenDto = userService.login(request);

        assertEquals("accessToken", jwtTokenDto.getAccessToken());
        assertEquals("refreshToken", jwtTokenDto.getRefreshToken());
    }

    @Test
    @DisplayName("잘못된 비밀번호발생시 예외발생")
    public void testLoginException() {
        JwtTokenLoginRequest request = new JwtTokenLoginRequest("testRealId", "wrongPassword");

        Users user = Users.builder()
                .userRealId(request.getUserRealId())
                .password("encodedPassword")
                .role(UserRole.USER)
                .build();

        when(userRepository.findByUserRealId(request.getUserRealId())).thenReturn(Optional.of(user));
        when(bCryptPasswordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(false);

        Exception exception = assertThrows(PasswordNotMatchException.class, () -> {
            userService.login(request);
        });

        assertEquals("잘못된 비밀번호입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("모든 회원조회")
    public void testFindAll() {
        UsersInfo usersInfo = UsersInfo.builder().build();

        Users user1 = Users.builder().userRealId("user1").usersInfoList(new ArrayList<>()).build();
        Users user2 = Users.builder().userRealId("user2").usersInfoList(new ArrayList<>()).build();

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<UserResponseDto> allUsers = userService.findAll();

        assertEquals(2, allUsers.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("아이디 중복확인")
    public void testIsDuplicateUserRealId() {
        when(userRepository.existsByUserRealId("duplicateRealId")).thenReturn(true);

        boolean isDuplicate = userService.isDuplicateUserRealId("duplicateRealId");

        assertTrue(isDuplicate);
    }
}
