package com.social.modern.exception;

import com.social.modern.dto.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(PrivateProfileException.class)
    public ResponseEntity<ErrorResponseDto> handlePrivateProfile(PrivateProfileException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(InvalidSettingException.class)
    public ResponseEntity<Void> handleInvalidSetting(InvalidSettingException ex) {
        return ResponseEntity.badRequest().build();
    }
}
