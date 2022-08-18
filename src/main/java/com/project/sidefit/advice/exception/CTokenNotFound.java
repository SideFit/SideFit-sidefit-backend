package com.project.sidefit.advice.exception;

/**
 * 해당 에러 응답시 인증 메일 재전송
 */
public class CTokenNotFound extends RuntimeException {
    public CTokenNotFound() {
    }

    public CTokenNotFound(String message) {
        super(message);
    }

    public CTokenNotFound(String message, Throwable cause) {
        super(message, cause);
    }
}
