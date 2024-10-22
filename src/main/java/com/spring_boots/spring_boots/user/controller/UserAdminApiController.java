package com.spring_boots.spring_boots.user.controller;

import com.spring_boots.spring_boots.user.domain.Users;
import com.spring_boots.spring_boots.user.dto.UserDto;
import com.spring_boots.spring_boots.user.dto.request.AdminCodeRequestDto;
import com.spring_boots.spring_boots.user.dto.request.AdminGrantTokenRequestDto;
import com.spring_boots.spring_boots.user.dto.response.*;
import com.spring_boots.spring_boots.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.spring_boots.spring_boots.config.jwt.UserConstants.ACCESS_TOKEN_TYPE_VALUE;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserAdminApiController {

    private final UserService userService;

    //모든 회원 정보 조회(관리자)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users")
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
//            ,@RequestParam(value = "keyword", defaultValue = "") String keyword
    ) {
//        List<UserResponseDto> users = userService.findAll();
        Page<UserResponseDto> result=userService.getUsersByCreatedAt(page,size);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    //관리자 본인인지아닌지
    @PreAuthorize(("hasRole('ADMIN')"))
    @GetMapping("/admin/user/principal")
    public ResponseEntity<UserPrincipalAdminResponseDto> checkPrincipalAdmin(UserDto userDto) {
        Long userId = userDto.getUserId();
        return ResponseEntity.status(HttpStatus.OK).body(
                UserPrincipalAdminResponseDto.builder().userId(userId).build());
    }

    //특정 회원 정보 조회(관리자)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users/{user_id}")
    public ResponseEntity<UserResponseDto> getUserByAdmin(@PathVariable("user_id") Long userId) {
        Users findUser = userService.findById(userId);
        UserResponseDto responseDto = findUser.toResponseDto();
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

//    //관리자 부여
    @PatchMapping("/admin/grant/{userId}")
    public ResponseEntity<AdminGrantTokenResponseDto> grantAdmin(@PathVariable("userId") Long userId,
                                                                 @RequestBody AdminGrantTokenRequestDto adminGrantTokenRequestDto) {
        Users authUser = userService.findById(userId);

        if (authUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AdminGrantTokenResponseDto.builder()
                            .message("인증되지 않은 사용자입니다. 로그인해주세요").build());
        }
        userService.grantRole(authUser, adminGrantTokenRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body(AdminGrantTokenResponseDto.builder()
                .message("권한부여 성공!").build());
    }

    //관리자 확인 API
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/admin-check")
    public ResponseEntity<UserCheckAdminResponseDto> checkAdmin(@CookieValue(value = ACCESS_TOKEN_TYPE_VALUE, required = false) String accessToken) {
        //accessToken 이 없는 경우
        if (accessToken == null || accessToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(UserCheckAdminResponseDto.builder()
                            .message("현재 엑세스 토큰이 없습니다.").build());
        }

        try {
            boolean isAdmin = userService.validateAdminToken(accessToken);

            if (isAdmin) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(UserCheckAdminResponseDto.builder().message("관리자 인증 성공").build());
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(UserCheckAdminResponseDto.builder().message("관리자 인증 실패").build());
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(UserCheckAdminResponseDto.builder().message("유효하지않은 토큰").build());
        }
    }

    //관리자 코드 체크 API
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/grant")
    public ResponseEntity<AdminCodeResponseDto> checkAdminCode(@RequestBody AdminCodeRequestDto adminCodeDto) {
        if (userService.checkAdminCode(adminCodeDto)) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(AdminCodeResponseDto.builder().message("success").build());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AdminCodeResponseDto.builder().message("fail").build());
        }
    }
}
