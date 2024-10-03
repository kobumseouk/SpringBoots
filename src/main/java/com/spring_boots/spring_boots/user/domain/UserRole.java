package com.spring_boots.spring_boots.user.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserRole {
    USER("User"),
    ADMIN("Admin");

    private final String roleName;
}
