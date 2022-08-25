package com.project.sidefit.api.controller.security;

import com.project.sidefit.api.dto.jwt.TokenRequestDto;
import com.project.sidefit.api.dto.response.Response;
import com.project.sidefit.api.dto.sign.*;
import com.project.sidefit.domain.service.dto.TokenDto;
import com.project.sidefit.domain.service.security.SignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


// TODO validation 에 대한 BindingResult 처리, type 안맞는 경우에 대한 예외처리 >> event 이용?
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class SignController {


    private final SignService signService;

    /**
     * 이메일 중복 확인
     */
    @GetMapping("/email/check")
    public Response checkEmailDuplicate(@RequestBody EmailRequestDto emailRequestDto) {
        if (signService.validateDuplicatedEmail(emailRequestDto.getEmail())) {
            // TODO 상태코드 번호 정하기, 현재 임시로 -1000 사용
            return Response.failure(-1000, "이미 존재하는 이메일입니다.");
        }

        return Response.success();
    }

    /**
     * validation 체크
     * User 에서 email 중복확인 >> 이 경우 예외 발생
     * UserPrev 에서 email 중복 확인 >> 이경우 bindingResult 에 정보 전체 정보 담아서 반환?
     * password == passwordCheck 인지 확인 >> bindingResult
     *
     * UserPrev에 email, pw 저장 
     */
    @PostMapping("/email/save")
    public Response saveEmailPassword(@Validated @RequestBody UserPrevRequestDto userPrevRequestDto, BindingResult bindingResult) {

        String email = userPrevRequestDto.getEmail();
        String password = userPrevRequestDto.getPassword();

        // TODO 제거해도 되는지?
        // UserJapRepo에서 email 중복 체크
        if (signService.validateDuplicatedEmail(email)) {
            // bindingResult 에 추가?
            return Response.failure(-1000, "이미 존재하는 이메일입니다.");
        }

        if (!password.equals(userPrevRequestDto.getPasswordCheck())) {
            return Response.failure(-1100, "패스워드가 일치하지 않습니다.");
        }


        /*if (bindingResult.hasErrors()) {
            log.info("검증 오류 발생 errors = {}", bindingResult);
            // 필요한 것만 뽑아서 전달
        }*/

        // UserPrev 에 저장
        signService.saveUserPrev(email, password);

        return Response.success();
    }

    /**
     * 이메일에 인증 링크 전송
     */
    @PostMapping("/email")
    public Response sendEmail(@RequestBody EmailRequestDto emailRequestDto) {
        signService.sendAuthEmail(emailRequestDto.getEmail());
        return Response.success();
    }

    /**
     * 이메일 인증처리
     * UserPrev 의 enable = true 로 변경
     *
     * TODO 현재 이메일에서 해당 링크 누르면 json 형태의 결과가 나옴 >> 이 부분 처리 필요
     * TODO Post?
     * 문제 발생 >> 크롬에서 링크를 눌렀을 경우 정상처리는 되지만 error 발생, postman 에서 테스트 한 경우는 정상동작
     */
    @GetMapping("/confirm-email/{token}")
    public Response emailAuth(@PathVariable String token) {
        // token 부분을 uuid 에서 인증토큰으로 변경
        // 토큰용 entity 생성 >> 추후 Redis 로 수정시 해결
        signService.confirmEmail(token);
        return Response.success();
    }

    /**
     * 이메일 인증여부 확인
     */
    @GetMapping("/email/auth/check")
    public Response checkEmailAuth(@RequestBody EmailRequestDto emailRequestDto) {
        if (signService.checkEmailAuth(emailRequestDto.getEmail())) {
            return Response.success();
        }

        return Response.failure(1000, "이메일 인증이 되지 않았습니다.");
    }


    /**
     * 이메일 재전송 api
     */
    @PostMapping("/email/again")
    public Response sendEmailAgain(@RequestBody EmailRequestDto emailRequestDto) {

        // TODO 이미 이메일 인증처리 되었으면 다른 결과 보내고 프론트에서 처리

        signService.sendAuthEmailAgain(emailRequestDto.getEmail());
        return Response.success();
    }


    /**
     * 닉네임 중복 체크 api
     */
    @GetMapping("/nickname/check")
    public Response checkNicknameDuplicate(@RequestBody NicknameRequestDto nicknameRequestDto) {

        if (signService.validateDuplicatedNickname(nicknameRequestDto.getNickname())) {
            return Response.failure(-1000, "이미 존재하는 닉네임입니다.");
        }

        return Response.success();
    }


    /**
     * 이메일 인증이 완료된 후 최종 회원가입
     * 1. email 로 UserPrev 찾는다.
     * 2. UserPrev, UserJoinRequest 조합해서 User 생성
     * 3. UserPrev 삭제
     * 4. User 저장
     */
    @PostMapping("/join")
    public Response join(@RequestBody UserJoinRequest userJoinRequest) {
        // 임시 테이블에서 email 로 정보 조회
        // 조회한 데이터와 넘어온 데이터 이용해서 User 생성, 저장
        signService.join(userJoinRequest.getEmail(), userJoinRequest.getNickname(), userJoinRequest.getJob());
        return Response.success();
    }


    /**
     * 이메일 로그인 api
     */
    @PostMapping("/login")
    public Response login(@RequestBody UserLoginRequestDto userLoginRequestDto) {
        // TokenDto 발급
        TokenDto tokenDto = signService.login(userLoginRequestDto.getEmail(), userLoginRequestDto.getPassword());
        return Response.success(tokenDto);
    }

    /**
     * Token 재발급 api
     */
    @PostMapping("/reissue")
    public Response reissue(@RequestBody TokenRequestDto tokenRequestDto) {

        return Response.success(signService.reissue(tokenRequestDto.getAccessToken(), tokenRequestDto.getRefreshToken()));
    }
}