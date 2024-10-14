package com.spring_boots.spring_boots.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
public class AdminGrantTokenResponseDto {
    private String message;
}
