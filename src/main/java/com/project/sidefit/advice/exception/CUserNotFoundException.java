package com.project.sidefit.advice.exception;

public class CUserNotFoundException extends RuntimeException {
    public CUserNotFoundException() {
    }

    public CUserNotFoundException(String message) {
        super(message);
    }

    public CUserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
