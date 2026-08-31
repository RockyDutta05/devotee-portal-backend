package com.devoteeportal.backend.exception;

public class ReferralLimitExceededException extends RuntimeException {
    public ReferralLimitExceededException(String message) {
        super(message);
    }
}
