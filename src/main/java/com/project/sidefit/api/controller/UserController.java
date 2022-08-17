package com.project.sidefit.api.controller;

import com.project.sidefit.api.dto.response.Response;
import com.project.sidefit.domain.entity.User;
import com.project.sidefit.domain.service.UserService;
import com.project.sidefit.domain.service.dto.UserDetailDto;
import com.project.sidefit.domain.service.dto.UserListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 전체 회원 목록 조회
     */
    @GetMapping("/users")
    public Response findAll() {

        List<UserListDto> users = userService.findAll();

        return Response.success(users);
    }

    /**
     * 회원 상세 조회
      */
    @GetMapping("/user/{id}")
    public Response findDetail(@PathVariable Long id) {
        UserDetailDto user = userService.findDetail(id);

        return Response.success(user);
    }


    /**
     * 테스트용
     */
    @PostMapping("/test-save")
    public Response save(@AuthenticationPrincipal User user) {

        return Response.success(userService.save(user));
    }

    /**
     * 비밀번호 변경
     */
//    @PatchMapping("/user/password")
//    public Response updatePassword() {
//
//    }
}