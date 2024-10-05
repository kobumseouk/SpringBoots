package com.spring_boots.spring_boots.user.controller;

import com.spring_boots.spring_boots.user.domain.Users;
import com.spring_boots.spring_boots.user.dto.request.UserSignupRequestDto;
import com.spring_boots.spring_boots.user.dto.response.UserResponseDto;
import com.spring_boots.spring_boots.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
public class UsersApiController {

    private final UserService userService;

    //회원가입
    @PostMapping("/v1/signup")
    public ResponseEntity<Users> signup(@RequestBody UserSignupRequestDto userSignupRequestDto) {

        if (userSignupRequestDto == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Users user = userService.save(userSignupRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    //모든 회원 정보 조회(관리자)
    @GetMapping("/v1/admin/users")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<UserResponseDto> users = userService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(users);
    }

    //특정 회원 정보 조회(관리자)
    @GetMapping("/v1/admin/users/{user_id}")
    public ResponseEntity<UserResponseDto> getUserByAdmin(@PathVariable("user_id") Long userId) {
        Users findUser = userService.findById(userId);
        UserResponseDto responseDto = findUser.toResponseDto();
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    //개인 정보 조회
    @GetMapping("/v1/users-info")
    public ResponseEntity<UserResponseDto> getUser() {
        // SecurityContext에서 인증 정보(Authentication)를 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            // principal이 Users 객체일 때만 캐스팅
            if (principal instanceof Users) {
                Users user = (Users) principal;
                log.info("유저 아이디: {}", user.getUserRealId());
                log.info("유저 이메일: {}", user.getEmail());
                UserResponseDto responseDto = user.toResponseDto();
                return ResponseEntity.status(HttpStatus.OK).body(responseDto);
            } else if (principal instanceof String) {
                // principal이 String이면 (JWT 인증 시 보통 username이 담김)
                String username = (String) principal;
                log.info("유저 이름(혹은 아이디): {}", username);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            } else {
                log.warn("알 수 없는 타입의 principal: {}", principal.getClass().getName());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    //회원 정보 수정
//    @PutMapping("/v1/users/")

    //회원 탈퇴

    //비밀번호 확인

    //아이디 중복확인
}
