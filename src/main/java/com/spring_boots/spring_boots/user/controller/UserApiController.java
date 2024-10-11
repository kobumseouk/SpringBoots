package com.spring_boots.spring_boots.user.controller;

import com.spring_boots.spring_boots.user.domain.Users;
import com.spring_boots.spring_boots.user.dto.request.UserPasswordRequestDto;
import com.spring_boots.spring_boots.user.dto.request.UserSignupRequestDto;
import com.spring_boots.spring_boots.user.dto.request.UserUpdateRequestDto;
import com.spring_boots.spring_boots.user.dto.response.UserPasswordResponseDto;
import com.spring_boots.spring_boots.user.dto.response.UserResponseDto;
import com.spring_boots.spring_boots.user.dto.response.UserSignupResponseDto;
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
                    .body(UserSignupResponseDto.builder()
                                    .message("잘못된 요청입니다.")
                                    .build());
        }

        Users user = userService.save(userSignupRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UserSignupResponseDto.builder()
                        .message("성공적으로 회원가입하셨습니다.")
                        .build());
    }

    //개인 정보 조회
    @GetMapping("/users-info")
    public ResponseEntity<UserResponseDto> getUser(@AuthenticationPrincipal Users user) {
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
/*
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
* */

    //회원 정보 수정
    @PutMapping("/users")
    public ResponseEntity<Users> updateUser(@AuthenticationPrincipal Users user,
                                            @RequestBody UserUpdateRequestDto request) {
        Users authUser = userService.findById(user.getUserId());    //인증객체 가져올시 영속성컨텍스트에서 가져와야함

        userService.update(authUser, request);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    //회원 탈퇴(hard delete)
    @DeleteMapping("/users-hard")
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal Users user,
                                           HttpServletResponse response) {
        Users authUser = userService.findById(user.getUserId());
        userService.deleteUser(authUser);

        if (userService.isDeleteUser(authUser)) {
            Cookie cookie = new Cookie("refreshToken", null);
//            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(0); // 쿠키 즉시 만료
            response.addCookie(cookie);

            return ResponseEntity.status(HttpStatus.OK).build();
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    //회원 탈퇴(soft delete)
    @DeleteMapping("/users-soft/{id}")
    public ResponseEntity<Void> softDeleteUser(@AuthenticationPrincipal Users user,
                                               @PathVariable Long id,
                                               HttpServletResponse response) {
        Users authUser = userService.findById(user.getUserId());
        userService.softDeleteUser(authUser);

        if (authUser.isDeleted()) {
            Cookie cookie = new Cookie("refreshToken", null);
//            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(0); // 쿠키 즉시 만료
            response.addCookie(cookie);

            return ResponseEntity.status(HttpStatus.OK).build();
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    //비밀번호 확인
    @PostMapping("/users/check-password")
    public ResponseEntity<UserPasswordResponseDto> checkPassword(@AuthenticationPrincipal Users user,
                                                                 @RequestBody UserPasswordRequestDto request) {
        Users authUser = userService.findById(user.getUserId());

        if (userService.checkPassword(authUser, request)) {
            return ResponseEntity.status(HttpStatus.OK).body(
                    UserPasswordResponseDto
                            .builder()
                            .id(authUser.getUserId())
                            .build());
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                UserPasswordResponseDto
                        .builder()
                        .build());
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
        deleteCookie("refreshToken",response);
        deleteCookie("accessToken",response);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    private void deleteCookie(String token,HttpServletResponse response) {

        Cookie cookie = new Cookie(token, null);
//        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // 쿠키 즉시 만료
        response.addCookie(cookie);
    }

    //아이디 중복확인
    @GetMapping("/signup/check-id")
    public ResponseEntity<Void> checkUsername(@RequestParam("userRealId") String userRealId) {
        boolean isUsernameTaken = userService.isDuplicateUserRealId(userRealId);

        if (isUsernameTaken) {
            // 아이디가 이미 존재하는 경우
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // 아이디 사용 가능
        return ResponseEntity.ok().build();
    }
}
