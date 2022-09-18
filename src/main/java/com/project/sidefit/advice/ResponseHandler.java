package com.project.sidefit.advice;

import com.project.sidefit.advice.exception.type.ExceptionType;
import com.project.sidefit.api.dto.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResponseHandler {

    private final MessageSource messageSource;

    // 인자 없는 에러
    public Response getFailureResponse(ExceptionType exceptionType) {
        return Response.failure(getCode(exceptionType.getCode()), getMessage(exceptionType.getMessage()));
    }

    // 인자 있는 에러
    public Response getFailureResponse(ExceptionType exceptionType, Object... args) {
        return Response.failure(getCode(exceptionType.getCode()), getMessage(exceptionType.getMessage(), args));
    }

    // error code 추출
    private Integer getCode(String key) {
        return Integer.valueOf(messageSource.getMessage(key, null, null));
    }

    // 인자 없는 error message 추출
    private String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    // 인자 있는 error message 추출
    private String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
}
