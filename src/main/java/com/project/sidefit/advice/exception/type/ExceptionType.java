package com.project.sidefit.advice.exception.type;

import lombok.Getter;

@Getter
public enum ExceptionType {

    EXCEPTION("exception.code", "exception.msg");

    private final String code;
    private final String message;

    ExceptionType(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
