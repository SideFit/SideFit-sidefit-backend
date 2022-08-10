package com.project.sidefit.advice.exception;

public class CEmailSignupFailedException extends RuntimeException {
    public CEmailSignupFailedException() {
    }

    public CEmailSignupFailedException(String message) {
        super(message);
    }

    public CEmailSignupFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
