package com.spring_boots.spring_boots.common.config;

import com.spring_boots.spring_boots.common.config.error.BadRequestException;
import com.spring_boots.spring_boots.common.config.error.ErrorResponseDto;
import com.spring_boots.spring_boots.common.config.error.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ErrorResponseDto> handleIllegalStateException(IllegalStateException ex) {
    log.error("잘못된 상태: {}", ex.getMessage(), ex);
    ErrorResponseDto errorResponse = new ErrorResponseDto("잘못된_상태", ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponseDto> handleResourceNotFoundException(ResourceNotFoundException ex) {
    log.error("리소스를 찾을 수 없음: {}", ex.getMessage(), ex);
    ErrorResponseDto errorResponse = new ErrorResponseDto(ex.getErrorCode(), ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  // 인증 예외 처리
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponseDto> handleAuthenticationException(AuthenticationException ex) {
    log.error("인증 실패: {}", ex.getMessage(), ex);
    ErrorResponseDto errorResponse = new ErrorResponseDto("인증_실패", "인증에 실패했습니다.");
    return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
  }

  // 권한 예외 처리
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponseDto> handleAccessDeniedException(AccessDeniedException ex) {
    log.error("접근 거부: {}", ex.getMessage(), ex);
    ErrorResponseDto errorResponse = new ErrorResponseDto("접근_거부", "접근 권한이 없습니다.");
    return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ErrorResponseDto> handleBadRequestException(BadRequestException ex) {
    log.error("잘못된 요청: {}", ex.getMessage(), ex);
    ErrorResponseDto errorResponse = new ErrorResponseDto(ex.getErrorCode(), ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponseDto> handleGeneralException(Exception ex) {
    log.error("예상치 못한 오류 발생: {}", ex.getMessage(), ex);
    ErrorResponseDto errorResponse = new ErrorResponseDto("서버_오류", "서버에서 오류가 발생했습니다.");
    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  // 유효성 검사 예외 처리
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponseDto> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    log.error("유효성 검사 실패: {}", ex.getMessage(), ex);
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach((error) -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });

    String errorMessage = errors.entrySet().stream()
        .map(entry -> entry.getKey() + ": " + entry.getValue())
        .collect(Collectors.joining(", "));

    ErrorResponseDto errorResponse = new ErrorResponseDto("유효성_검사_실패", errorMessage);
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(IOException.class)
  public ResponseEntity<ErrorResponseDto> handleIOException(IOException ex) {
    log.error("입출력 예외 발생: {}", ex.getMessage(), ex);
    ErrorResponseDto errorResponse = new ErrorResponseDto("파일_처리_오류", "파일 처리 중 오류가 발생했습니다.");
    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

}


