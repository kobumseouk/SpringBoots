package com.spring_boots.spring_boots.user.controller;

import com.spring_boots.spring_boots.user.domain.Users;
import com.spring_boots.spring_boots.user.dto.request.UserSignupRequestDto;
import com.spring_boots.spring_boots.user.dto.response.UserResponseDto;
import com.spring_boots.spring_boots.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
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

    //특정 회원 정보 조회

    //회원 정보 수정

    //회원 탈퇴

    //비밀번호 확인

    //아이디 중복확인
}
