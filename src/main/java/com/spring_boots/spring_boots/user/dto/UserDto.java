package com.spring_boots.spring_boots.user.dto;

import com.spring_boots.spring_boots.orders.entity.Orders;
import com.spring_boots.spring_boots.user.domain.Provider;
import com.spring_boots.spring_boots.user.domain.UserRole;
import com.spring_boots.spring_boots.user.domain.UsersInfo;
import lombok.*;

import java.util.List;

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
    private String password;
    private UserRole role;
    private Provider provider;
}
