package com.spring_boots.spring_boots.user.controller;

import com.spring_boots.spring_boots.user.domain.Provider;
import com.spring_boots.spring_boots.user.domain.Users;
import com.spring_boots.spring_boots.user.dto.UserDto;
import com.spring_boots.spring_boots.user.dto.request.UserPasswordRequestDto;
import com.spring_boots.spring_boots.user.dto.request.UserSignupRequestDto;
import com.spring_boots.spring_boots.user.dto.request.UserUpdateRequestDto;
import com.spring_boots.spring_boots.user.dto.response.*;
import com.spring_boots.spring_boots.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
public class UserApiController {

    private final UserService userService;

    //회원가입
    @PostMapping("/signup")
    public ResponseEntity<UserSignupResponseDto> signup(@RequestBody UserSignupRequestDto userSignupRequestDto) {

        if (userSignupRequestDto == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(UserSignupResponseDto.builder().message("잘못된 요청입니다.").build());
        }

        Users user = userService.save(userSignupRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UserSignupResponseDto.builder().message("성공적으로 회원가입하셨습니다.").build());
    }

    //개인 정보 조회
    @GetMapping("/users-info")
    public ResponseEntity<UserResponseDto> getUser(UserDto user) {
        try {
            Users authUser = userService.findById(user.getUserId());

            if (authUser != null) {
                UserResponseDto responseDto = authUser.toResponseDto();
                return ResponseEntity.status(HttpStatus.OK).body(responseDto);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    //회원 정보 수정(이름, 아이디는 변경 불가능)
    @PatchMapping("/users/{userInfoId}")
    public ResponseEntity<UserUpdateResponseDto> updateUser(@AuthenticationPrincipal Users user,
                                                            @PathVariable("userInfoId") Long userInfoId,
                                                            @RequestBody UserUpdateRequestDto request) {
        try {
            Users authUser = userService.findById(user.getUserId());    //인증객체 가져올시 영속성컨텍스트에서 가져와야함

            if (authUser.getProvider().equals(Provider.NONE)) {
                userService.updateNoneUser(authUser, request, userInfoId);
            } else {
                userService.updateGoogleUser(authUser, request, userInfoId);
            }

            return ResponseEntity.status(HttpStatus.OK).body(UserUpdateResponseDto
                    .builder().message("정상적으로 수정되었습니다.").build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(UserUpdateResponseDto
                    .builder().message("잘못된 데이터 요청입니다.").build());        }
    }

//    //회원 탈퇴(hard delete)
//    @DeleteMapping("/users-hard")
//    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal Users user,
//                                           HttpServletResponse response) {
//        Users authUser = userService.findById(user.getUserId());
//        userService.deleteUser(authUser);
//
//        if (userService.isDeleteUser(authUser)) {
//            Cookie cookie = new Cookie("refreshToken", null);
////            cookie.setHttpOnly(true);
//            cookie.setPath("/");
//            cookie.setMaxAge(0); // 쿠키 즉시 만료
//            response.addCookie(cookie);
//
//            return ResponseEntity.status(HttpStatus.OK).build();
//        }
//
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
//    }

    //회원 탈퇴(soft delete)
    @DeleteMapping("/users-soft/{id}")
    public ResponseEntity<UserDeleteResponseDto> softDeleteUser(@AuthenticationPrincipal Users user,
                                                                @PathVariable Long id,
                                                                HttpServletResponse response) {
        Users authUser = userService.findById(user.getUserId());
        userService.softDeleteUser(authUser);

        if (authUser.isDeleted()) {
            deleteCookie("refreshToken", response);
            deleteCookie("accessToken", response);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(UserDeleteResponseDto.builder().message("회원탈퇴 성공").build());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(UserDeleteResponseDto.builder().message("오류 발생").build());
    }

    //비밀번호 확인
    @PostMapping("/users/check-password")
    public ResponseEntity<UserPasswordResponseDto> checkPassword(@AuthenticationPrincipal Users user,
                                                                 @RequestBody UserPasswordRequestDto request) {
        Users authUser = userService.findById(user.getUserId());

        if (userService.checkPassword(authUser, request)) {
            return ResponseEntity.status(HttpStatus.OK).body(
                    UserPasswordResponseDto.builder()
                            .id(authUser.getUserId())
                            .build());
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                UserPasswordResponseDto.builder().build());
    }

    //로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response,
                                       @AuthenticationPrincipal Users user) {
        Users authUser = userService.findById(user.getUserId());
        if (authUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        //엑세스토큰, 리프레시토큰 삭제
        deleteCookie("refreshToken", response);
        deleteCookie("accessToken", response);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    //아이디 중복확인
    @GetMapping("/signup/check-id")
    public ResponseEntity<UserCheckIdResponseDto> checkUsername(@RequestParam("userRealId") String userRealId) {
        boolean isUsernameTaken = userService.isDuplicateUserRealId(userRealId);

        if (isUsernameTaken) {
            // 아이디가 이미 존재하는 경우
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(UserCheckIdResponseDto.builder()
                            .isAvailable(false).message("이미 사용중인 아이디입니다.").build());
        }

        // 아이디 사용 가능
        return ResponseEntity.ok()
                .body(UserCheckIdResponseDto.builder()
                        .isAvailable(true).message("사용할 수 있는 아이디입니다.").build());
    }

    //쿠키 삭제 로직
    private void deleteCookie(String token, HttpServletResponse response) {

        Cookie cookie = new Cookie(token, null);
//        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // 쿠키 즉시 만료
        response.addCookie(cookie);
    }
}
