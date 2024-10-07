package com.spring_boots.spring_boots.user.controller;

import com.spring_boots.spring_boots.user.domain.Users;
import com.spring_boots.spring_boots.user.dto.request.AdminGrantTokenRequestDto;
import com.spring_boots.spring_boots.user.dto.response.UserResponseDto;
import com.spring_boots.spring_boots.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserAdminApiController {

    private final UserService userService;

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

    //관리자 부여
    @PatchMapping("/v1/admin/grant")
    public ResponseEntity<String> grantAdmin(@AuthenticationPrincipal Users user,
                                           @RequestBody AdminGrantTokenRequestDto adminGrantTokenRequestDto) {
        Users authUser = userService.findByEmail(user.getEmail());

        if (authUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증되지 않은 사용자입니다. 로그인해주세요");
        }

        if (userService.grantAdminToken(authUser, adminGrantTokenRequestDto) && userService.isGrantAdmin(authUser)) {
            return ResponseEntity.status(HttpStatus.OK).build();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("잘못된 토큰입니다.");
        }
    }
}
