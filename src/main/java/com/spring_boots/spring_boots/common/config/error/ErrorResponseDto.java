package com.spring_boots.spring_boots.common.config.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ErrorResponseDto {
  private final String errorCode;
  private final String errorMessage;
}
