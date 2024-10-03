package com.spring_boots.spring_boots.user.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponseDto {
    private String username;
    private String userRealId;
    private String email;
}
