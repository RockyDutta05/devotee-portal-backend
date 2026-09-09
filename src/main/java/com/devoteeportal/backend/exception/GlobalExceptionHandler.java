package com.devoteeportal.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PendingApprovalException.class)
    public ResponseEntity<Map<String, Object>> handlePendingApproval(PendingApprovalException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(RejectedApprovalException.class)
    public ResponseEntity<Map<String, Object>> handleRejectedApproval(RejectedApprovalException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ReferralLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleReferralLimitExceeded(ReferralLimitExceededException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(org.springframework.web.bind.MethodArgumentNotValidException ex) {
        StringBuilder sb = new StringBuilder();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            sb.append(((org.springframework.validation.FieldError) error).getField()).append(": ").append(error.getDefaultMessage()).append("; ");
        });
        return buildErrorResponse(sb.toString(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("not authorized")) {
            return buildErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN);
        }
        
        if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("not found")) {
            return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
        }

        if (ex instanceof org.springframework.security.core.AuthenticationException) {
            return buildErrorResponse("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

        return buildErrorResponse("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", message);
        response.put("status", status.value());
        return new ResponseEntity<>(response, status);
    }
}
