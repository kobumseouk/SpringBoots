package com.spring_boots.spring_boots.config.jwt;

public interface AuthToken <T>{
    String AUTHORITIES_TOKEN_KEY = "role";
    boolean validate();
    T getDate();
}
