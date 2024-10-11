package com.spring_boots.spring_boots.user.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UsersInfoResponseDto {
    private String address;
    private String streetAddress;
    private String detailedAddress;
    private String phone;
}
