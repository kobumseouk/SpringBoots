package com.spring_boots.spring_boots.common.config.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 404 Not Found Error
@Getter
public class ResourceNotFoundException extends RuntimeException {
  private final String errorCode;

  public ResourceNotFoundException(String message) {
    super(message);
    this.errorCode = "리소스_없음";
  }

  public ResourceNotFoundException(String message, String errorCode) {
    super(message);
    this.errorCode = errorCode;
  }

}
