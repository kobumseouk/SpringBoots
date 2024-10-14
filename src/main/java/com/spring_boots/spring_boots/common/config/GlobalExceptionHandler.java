package com.spring_boots.spring_boots.common.config;

import com.spring_boots.spring_boots.common.config.error.BadRequestException;
import com.spring_boots.spring_boots.common.config.error.ErrorResponseDto;
import com.spring_boots.spring_boots.common.config.error.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponseDto> handleResourceNotFoundException(ResourceNotFoundException ex) {
    ErrorResponseDto errorResponse = new ErrorResponseDto(ex.getErrorCode(), ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponseDto> handleAuthenticationException(AuthenticationException ex) {
    ErrorResponseDto errorResponse = new ErrorResponseDto("인증_실패", "인증에 실패했습니다.");
    return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponseDto> handleAccessDeniedException(AccessDeniedException ex) {
    ErrorResponseDto errorResponse = new ErrorResponseDto("접근_거부", "접근 권한이 없습니다.");
    return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ErrorResponseDto> handleBadRequestException(BadRequestException ex) {
    ErrorResponseDto errorResponse = new ErrorResponseDto(ex.getErrorCode(), ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponseDto> handleGeneralException(Exception ex) {
    log.error("Unexpected error occurred", ex);
    ErrorResponseDto errorResponse = new ErrorResponseDto("서버_오류", "서버에서 오류가 발생했습니다.");
    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }



}


