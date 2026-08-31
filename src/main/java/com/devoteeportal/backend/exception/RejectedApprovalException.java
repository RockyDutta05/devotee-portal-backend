package com.devoteeportal.backend.exception;

public class RejectedApprovalException extends RuntimeException {
    public RejectedApprovalException(String message) {
        super(message);
    }
}
