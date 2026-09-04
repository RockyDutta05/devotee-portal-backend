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
    public ResponseEntity<Map<String, String>> handlePendingApproval(PendingApprovalException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "403_PENDING_APPROVAL");
        response.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(RejectedApprovalException.class)
    public ResponseEntity<Map<String, String>> handleRejectedApproval(RejectedApprovalException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "403_REJECTED");
        response.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(ReferralLimitExceededException.class)
    public ResponseEntity<Map<String, String>> handleReferralLimitExceeded(ReferralLimitExceededException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "400_REFERRAL_LIMIT_EXCEEDED");
        response.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(org.springframework.web.bind.MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((org.springframework.validation.FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Bad Request");
        response.put("message", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> response = new HashMap<>();
        
        if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("not authorized")) {
            response.put("error", "Forbidden");
            response.put("message", ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }
        
        if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("not found")) {
            response.put("error", "Not Found");
            response.put("message", ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        if (ex instanceof org.springframework.security.core.AuthenticationException) {
            response.put("error", "Unauthorized");
            response.put("message", "Invalid email or password");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        response.put("error", "Internal Server Error");
        response.put("message", "An unexpected error occurred");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
