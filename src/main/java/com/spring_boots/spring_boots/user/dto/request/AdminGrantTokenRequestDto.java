package com.spring_boots.spring_boots.user.dto.request;

import com.spring_boots.spring_boots.user.domain.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminGrantTokenRequestDto {
    private UserRole roles;
}
