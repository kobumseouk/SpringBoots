package com.spring_boots.spring_boots.common.config.error;

import lombok.Getter;

// 400 Bad Request Error
@Getter
public class BadRequestException extends RuntimeException {
  private final String errorCode;

  public BadRequestException(String errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }
}
