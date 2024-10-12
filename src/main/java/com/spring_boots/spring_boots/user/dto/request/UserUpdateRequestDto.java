package com.spring_boots.spring_boots.user.dto.request;

import com.spring_boots.spring_boots.user.dto.response.UsersInfoResponseDto;
import lombok.Getter;

import java.util.List;

@Getter
public class UserUpdateRequestDto {
    private String currentPassword; //현재 비밀번호
    private String updatePassword;  //변경할 비밀번호
    private String email; //변경할 이메일
    private List<UsersInfoResponseDto> address;
}
