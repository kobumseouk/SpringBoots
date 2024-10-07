package com.spring_boots.spring_boots.user.controller;

import com.spring_boots.spring_boots.user.domain.Users;
import com.spring_boots.spring_boots.user.dto.response.UserResponseDto;
import com.spring_boots.spring_boots.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
