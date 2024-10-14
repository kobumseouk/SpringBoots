package com.spring_boots.spring_boots.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminCodeRequestDto {
    private String adminCode;
}
