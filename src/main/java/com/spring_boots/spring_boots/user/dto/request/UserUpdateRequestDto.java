package com.spring_boots.spring_boots.user.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
public class UserUpdateRequestDto {
    private String password;
    private String email;
}
