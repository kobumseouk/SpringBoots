package com.spring_boots.spring_boots.user.exception;

import java.sql.SQLException;

public class TokenNotFoundException extends RuntimeException {
    public TokenNotFoundException(String message) {
        super(message);
    }
}
