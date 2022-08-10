package com.project.sidefit.api.controller.security;

import com.project.sidefit.advice.exception.CAccessDeniedException;
import com.project.sidefit.advice.exception.CAuthenticationEntryPointException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exception")
public class ExceptionController {

    // 정상적으로 Jwt가 제대로 오지 않은 경우
    @GetMapping("/entryPoint")
    public void entrypointException() {
        throw new CAuthenticationEntryPointException();
    }

    // 정상적인 Jwt가 왔지만 권한이 다른경우
    @GetMapping("/accessDenied")
    public void accessDeniedException() {
        throw new CAccessDeniedException();
    }
}
