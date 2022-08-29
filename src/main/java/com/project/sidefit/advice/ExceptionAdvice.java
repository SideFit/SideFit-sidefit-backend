package com.project.sidefit.advice;

import com.project.sidefit.advice.exception.type.ExceptionType;
import com.project.sidefit.api.dto.response.NotValidResponse;
import com.project.sidefit.api.dto.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

import static com.project.sidefit.advice.exception.type.ExceptionType.*;


@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ExceptionAdvice {

    private final ResponseHandler responseHandler;

    /**
     * default Exception
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected Response defaultException(Exception e) {
        log.error("[exceptionHandler] ex", e);
        return getFailureResponse(EXCEPTION);
    }

    /**
     * validation
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<List<NotValidResponse>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("handleMethodArgumentNotValidException", e);
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        List<NotValidResponse> notValidResponseList = new ArrayList<>();

        for (FieldError fieldError : fieldErrors) {
            NotValidResponse notValidResponse = NotValidResponse.builder()
                    .defaultMessage(fieldError.getDefaultMessage())
                    .field(fieldError.getField())
                    .rejectedValue(fieldError.getRejectedValue())
                    .code(fieldError.getCode())
                    .build();
            notValidResponseList.add(notValidResponse);
        }
        return new ResponseEntity<>(notValidResponseList, HttpStatus.BAD_REQUEST);
    }

    private Response getFailureResponse(ExceptionType exceptionType) {
        return responseHandler.getFailureResponse(exceptionType);
    }

    private Response getFailureResponse(ExceptionType exceptionType, Object... args) {
        return responseHandler.getFailureResponse(exceptionType, args);
    }
}
