package com.project.sidefit.api.controller;

import com.project.sidefit.api.dto.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HealthcheckController {

    @GetMapping("/healthcheck")
    public Response healthcheck() {

        return Response.success("service is health");
    }

    // 로그인 동작하는지 확인용 컨트롤러
    @GetMapping("/login/check")
    public Response loginCheck() {
        return Response.success("로그인 연결 동작 성공");
    }
}
