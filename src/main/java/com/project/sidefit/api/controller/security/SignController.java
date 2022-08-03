package com.project.sidefit.api.controller.security;

import com.project.sidefit.api.dto.response.Response;
import com.project.sidefit.config.security.JwtProvider;
import com.project.sidefit.domain.service.dto.TokenDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/sidefit")
@RequiredArgsConstructor
public class SignController {

    private final JwtProvider jwtProvider;

    // jwt 발급 테스트용 api
    @GetMapping("/jwt-test")
    public Response jwtTest() {
        List<String> roles = new ArrayList<>();
        roles.add("ROLE_USER");

        TokenDto tokenDto = jwtProvider.createTokenDto(1L, roles);

        return Response.success(tokenDto);
    }

    // 인증필터 적용 확인용
    @GetMapping("/token-validation")
    public Response jwtValidation() {
        return Response.success("token validation test");
    }
}
