package com.spring_boots.spring_boots.user.dto.response;

import com.spring_boots.spring_boots.user.domain.Provider;
import com.spring_boots.spring_boots.user.domain.UserRole;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class UserResponseDto {
    private Long userId;
    private String username;
    private String userRealId;
    private String email;
    private UserRole role;
    private Provider provider;
    private LocalDateTime createdAt;
    private List<UsersInfoResponseDto> userInfoList;
    private String message;
}
