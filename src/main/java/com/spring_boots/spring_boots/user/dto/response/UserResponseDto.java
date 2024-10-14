package com.spring_boots.spring_boots.user.dto.response;

import com.spring_boots.spring_boots.user.domain.UserRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponseDto {
    private String username;
    private String userRealId;
    private String email;
    private UserRole role;
}
