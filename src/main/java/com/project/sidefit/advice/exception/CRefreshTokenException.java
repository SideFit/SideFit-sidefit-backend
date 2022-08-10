package com.project.sidefit.advice.exception;

public class CRefreshTokenException extends RuntimeException {
    public CRefreshTokenException() {
    }

    public CRefreshTokenException(String message) {
        super(message);
    }

    public CRefreshTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
