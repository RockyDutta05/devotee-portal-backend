package com.devoteeportal.backend.exception;

public class PendingApprovalException extends RuntimeException {
    public PendingApprovalException(String message) {
        super(message);
    }
}
