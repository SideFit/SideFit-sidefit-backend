package com.project.sidefit.api.controller;

import com.project.sidefit.api.dto.PasswordRequestDto;
import com.project.sidefit.api.dto.response.Response;
import com.project.sidefit.api.dto.sign.EmailRequestDto;
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
     * 프로필 수정
     */
//    @PatchMapping("/user/{id}")
//    public Response updateUser(@PathVariable Long id) {
//
//    }

    /**
     * 테스트용
     */
    @PostMapping("/test-save")
    public Response save(@AuthenticationPrincipal User user) {

        return Response.success(userService.save(user));
    }










    /**
     * 비밀번호 변경 메일
     */
    @PostMapping("/user/password/email")
    public Response sendPasswordEmail(@RequestBody EmailRequestDto emailRequestDto) {

        userService.sendPasswordEmail(emailRequestDto.getEmail());
        return Response.success();
    }

    /**
     * 비밀번호 변경 처리
     *
     *
     */
    @PatchMapping("/user/password/{token}")
    public Response updatePassword(@PathVariable String token, @RequestBody PasswordRequestDto passwordRequestDto) {

        // password, passwordCheck 같은지 체크
        if (!passwordRequestDto.getPassword().equals(passwordRequestDto.getPasswordCheck())) {
            // bindingResult 에 담기?
            return Response.failure(-1000, "입력한 패스워드가 일치하지 않습니다.");
        }

        // token 을 통해서 user 알아내기
        // 해당 user의 pw 를 변경
        userService.updatePassword(token, passwordRequestDto.getPassword());

        return Response.success();
    }
}