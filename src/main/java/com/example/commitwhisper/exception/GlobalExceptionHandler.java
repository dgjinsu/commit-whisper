package com.example.commitwhisper.exception;

import com.example.commitwhisper.dto.common.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("IllegalArgumentException 발생: {}", e.getMessage());
        
        String message = e.getMessage();
        HttpStatus status;
        
        if (message != null) {
            if (message.contains("권한")) {
                status = HttpStatus.FORBIDDEN;
            } else if (message.contains("찾을 수 없습니다")) {
                status = HttpStatus.NOT_FOUND;
            } else {
                status = HttpStatus.BAD_REQUEST;
            }
        } else {
            status = HttpStatus.BAD_REQUEST;
        }
        
        return ResponseEntity.status(status).body(new ErrorResponse(message));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(UsernameNotFoundException e) {
        log.warn("UsernameNotFoundException 발생: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        log.error("RuntimeException 발생", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("서버 내부 오류가 발생했습니다."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("예상치 못한 예외 발생", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("서버 내부 오류가 발생했습니다."));
    }
}

