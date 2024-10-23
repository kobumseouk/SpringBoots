package com.spring_boots.spring_boots.user.controller;

import com.spring_boots.spring_boots.user.domain.Provider;
import com.spring_boots.spring_boots.user.domain.Users;
import com.spring_boots.spring_boots.user.dto.UserDto;
import com.spring_boots.spring_boots.user.dto.request.UserPasswordRequestDto;
import com.spring_boots.spring_boots.user.dto.request.UserSignupRequestDto;
import com.spring_boots.spring_boots.user.dto.request.UserUpdateRequestDto;
import com.spring_boots.spring_boots.user.dto.response.*;
import com.spring_boots.spring_boots.user.exception.PasswordNotMatchException;
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

import static com.spring_boots.spring_boots.config.jwt.UserConstants.ACCESS_TOKEN_TYPE_VALUE;
import static com.spring_boots.spring_boots.config.jwt.UserConstants.REFRESH_TOKEN_TYPE_VALUE;

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
        if (user == null) {
            return ResponseEntity.status(HttpStatus.OK).body(UserResponseDto.builder()
                    .message("사용자 정보없음.").build());
        }
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
    public ResponseEntity<UserUpdateResponseDto> updateUser(UserDto userDto,
                                                            @PathVariable("userInfoId") Long userInfoId,
                                                            @RequestBody UserUpdateRequestDto request) {
        try {

            if (userDto.getProvider().equals(Provider.NONE)) {
                userService.updateNoneUser(userDto, request, userInfoId);
            } else {
                userService.updateGoogleUser(userDto, request, userInfoId);
            }

            return ResponseEntity.status(HttpStatus.OK).body(UserUpdateResponseDto
                    .builder().message("정상적으로 수정되었습니다.").build());
        } catch (PasswordNotMatchException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(UserUpdateResponseDto
                    .builder().message("잘못된 데이터 요청입니다.").build());
        }
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
    public ResponseEntity<UserDeleteResponseDto> softDeleteUser(UserDto userDto,
                                                                @PathVariable Long id,
                                                                HttpServletResponse response) {
        UserDeleteResponseDto userDeleteResponseDto = userService.softDeleteUser(userDto);

        if (userDeleteResponseDto.isDeleted()) {
            deleteCookie(REFRESH_TOKEN_TYPE_VALUE, response);
            deleteCookie(ACCESS_TOKEN_TYPE_VALUE, response);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(UserDeleteResponseDto.builder().message("회원탈퇴 성공").build());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(UserDeleteResponseDto.builder().message("오류 발생").build());
    }

    //비밀번호 확인
    @PostMapping("/users/check-password")
    public ResponseEntity<UserPasswordResponseDto> checkPassword(UserDto userDto,
                                                                 @RequestBody UserPasswordRequestDto request) {
        //일반 회원의 경우
        if (userDto.getProvider().equals(Provider.NONE)) {
            if (userService.checkPassword(userDto, request)) {
                return ResponseEntity.status(HttpStatus.OK).body(
                        UserPasswordResponseDto.builder()
                                .id(userDto.getUserId())
                                .build());
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    UserPasswordResponseDto.builder().build());
        } else {
            //구글 로그인의 경우 비밀번호 인증이 필요없으므로 바로 리턴
            return ResponseEntity.status(HttpStatus.OK).body(
                    UserPasswordResponseDto.builder()
                            .id(userDto.getUserId())
                            .build());
        }
    }

    //로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response,
                                       UserDto user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        //엑세스토큰, 리프레시토큰 삭제
        deleteCookie(REFRESH_TOKEN_TYPE_VALUE, response);
        deleteCookie(ACCESS_TOKEN_TYPE_VALUE, response);

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

    //해당 유저가 구글인지 일반인지 확인
    @GetMapping("/provider")
    public ResponseEntity<UserProviderResponseDto> checkProvider(UserDto userDto) {
        return ResponseEntity.status(HttpStatus.OK).body(UserProviderResponseDto.builder()
                .provider(userDto.getProvider()).build());
    }

    //쿠키 삭제 로직
    private void deleteCookie(String token, HttpServletResponse response) {
        Cookie cookie = new Cookie(token, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setAttribute("SameSite", "Lax");
        cookie.setPath("/");
        cookie.setMaxAge(0); // 쿠키 즉시 만료
        response.addCookie(cookie);
    }
}
