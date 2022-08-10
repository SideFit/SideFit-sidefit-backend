package com.project.sidefit.advice.exception;

public class CEmailLoginFailedException extends RuntimeException {
    public CEmailLoginFailedException() {
    }

    public CEmailLoginFailedException(String message) {
        super(message);
    }

    public CEmailLoginFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
