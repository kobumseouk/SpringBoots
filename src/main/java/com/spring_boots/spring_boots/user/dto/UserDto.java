package com.spring_boots.spring_boots.user.dto;

import com.spring_boots.spring_boots.user.domain.UserRole;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long userId;
    private String username;
    private String userRealId;
    private String email;
    private UserRole role;
}
