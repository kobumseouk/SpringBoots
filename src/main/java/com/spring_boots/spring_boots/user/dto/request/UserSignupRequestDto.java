package com.spring_boots.spring_boots.user.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserSignupRequestDto {

    private String username;
    private String userRealId;
    private String email;
    private String password;

}
