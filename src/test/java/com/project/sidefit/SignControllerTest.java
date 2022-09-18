package com.project.sidefit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.sidefit.api.dto.sign.*;
import com.project.sidefit.domain.service.dto.TokenDto;
import com.project.sidefit.domain.service.auth.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.http.MediaType.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@AutoConfigureRestDocs
@SpringBootTest
public class SignControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService signService;

    /*@MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @MockBean
    private CustomAccessDeniedHandler customAccessDeniedHandler;*/


    @Test
    @DisplayName("이메일 중복 체크 : Post /api/auth/email/check")
    public void checkEmailDuplicate() throws Exception {
        //given
        given(signService.validateDuplicatedEmail(any(String.class))).willReturn(false);


        //when
        EmailRequestDto request = EmailRequestDto.builder().email("test@gmail.com").build();
        ResultActions result = this.mockMvc.perform(post("/api/auth/email/check")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("check_email_duplicate",
                        requestFields(
                                fieldWithPath("email").type(STRING).description("email")
                        ),
                        responseFields(
                                fieldWithPath("success").type(BOOLEAN).description("성공 여부"),
                                fieldWithPath("code").type(NUMBER).description("상태 코드"),
                                fieldWithPath("result").type(NULL).description("반환 데이터")
                        )
                ));

    }


    @Test
    @DisplayName("회원가입 1차 정보 저장 : Post /api/auth/email/save")
    public void userPrev() throws Exception {
        //given
        given(signService.validateDuplicatedEmail(any(String.class))).willReturn(false);
        given(signService.saveUserPrev(any(String.class), any(String.class))).willReturn(any(Long.class));

        //when
        UserPrevRequestDto request = UserPrevRequestDto.builder().email("test@gmail.com").password("1234").passwordCheck("1234").build();
        ResultActions result = this.mockMvc.perform(post("/api/auth/email/save")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("save_userPrev",
                        requestFields(
                                fieldWithPath("email").type(STRING).description("이메일"),
                                fieldWithPath("password").type(STRING).description("비밀번호"),
                                fieldWithPath("passwordCheck").type(STRING).description("비밀번호 확인")
                        ),
                        responseFields(
                                fieldWithPath("success").type(BOOLEAN).description("성공 여부"),
                                fieldWithPath("code").type(NUMBER).description("상태 코드"),
                                fieldWithPath("result").type(NULL).description("반환 데이터")
                        )
                ));
    }


    @Test
    @DisplayName("이메일에 인증링크 전송 : Post /api/auth/email")
    public void send_email() throws Exception {
        //given
        willDoNothing().given(signService).sendAuthEmail(anyString());

        //when
        EmailRequestDto request = EmailRequestDto.builder().email("test@gmail.com").build();
        ResultActions result = this.mockMvc.perform(post("/api/auth/email")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("send_email_auth",
                        requestFields(
                                fieldWithPath("email").type(STRING).description("이메일")
                        ),
                        responseFields(
                                fieldWithPath("success").type(BOOLEAN).description("성공 여부"),
                                fieldWithPath("code").type(NUMBER).description("상태 코드"),
                                fieldWithPath("result").type(NULL).description("반환 데이터")
                        )
                ));
    }

    @Test
    @DisplayName("이메일 인증 처리 : Get /api/auth/confirm-email/{token}")
    public void confirmEmail() throws Exception {
        //given
        willDoNothing().given(signService).confirmEmail(anyString());

        //when
        ResultActions result = this.mockMvc.perform(RestDocumentationRequestBuilders.get("/api/auth/confirm-email/{token}", "5b52fe61-6a3f-4910-9cb8-186e0ac65053")
                .accept(APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("confirm_email",
                        pathParameters(
                                parameterWithName("token").description("이메일 인증 토큰")
                        ),
                        responseFields(
                                fieldWithPath("success").type(BOOLEAN).description("성공 여부"),
                                fieldWithPath("code").type(NUMBER).description("상태 코드"),
                                fieldWithPath("result").type(NULL).description("반환 데이터")
                        )
                ));
    }

    @Test
    @DisplayName("이메일 인증 체크 : Post /api/auth/email/auth/check")
    public void emailCheck() throws Exception {
        //given
        EmailRequestDto request = EmailRequestDto.builder().email("test@gmail.com").build();
        given(signService.checkEmailAuth(any(String.class))).willReturn(true);

        //when
        ResultActions result = this.mockMvc.perform(post("/api/auth/email/auth/check")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("email_auth_check",
                        requestFields(
                                fieldWithPath("email").type(STRING).description("이메일")
                        ),
                        responseFields(
                                fieldWithPath("success").type(BOOLEAN).description("성공 여부"),
                                fieldWithPath("code").type(NUMBER).description("상태 코드"),
                                fieldWithPath("result").type(NULL).description("반환 데이터")
                        )
                ));
    }

    @Test
    @DisplayName("인증메일 재전송 : Post /api/auth/email/again")
    public void sendEmailAuthAgain() throws Exception {
        //given
        EmailRequestDto request = EmailRequestDto.builder().email("test@gmail.com").build();
        willDoNothing().given(signService).sendAuthEmailAgain(anyString());

        //when
        ResultActions result = this.mockMvc.perform(post("/api/auth/email/again")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("re-send_email_auth",
                        requestFields(
                                fieldWithPath("email").type(STRING).description("이메일")
                        ),
                        responseFields(
                                fieldWithPath("success").type(BOOLEAN).description("성공 여부"),
                                fieldWithPath("code").type(NUMBER).description("상태 코드"),
                                fieldWithPath("result").type(NULL).description("반환 데이터")
                        )
                ));
    }

    @Test
    @DisplayName("닉네임 중복 체크 : Post /api/auth/nickname/check")
    public void checkNicknameDuplicate() throws Exception {
        //given
        given(signService.validateDuplicatedNickname(anyString())).willReturn(false);

        //when
        NicknameRequestDto request = NicknameRequestDto.builder().nickname("testUser").build();
        ResultActions result = this.mockMvc.perform(post("/api/auth/nickname/check")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("check_nickname_duplicate",
                        requestFields(
                                fieldWithPath("nickname").type(STRING).description("닉네임")
                        ),
                        responseFields(
                                fieldWithPath("success").type(BOOLEAN).description("성공 여부"),
                                fieldWithPath("code").type(NUMBER).description("상태 코드"),
                                fieldWithPath("result").type(NULL).description("반환 데이터")
                        )
                ));
    }

    @Test
    @DisplayName("이메일 회원가입 : Post /api/auth/join")
    public void email_join() throws Exception {
        //given
        willDoNothing().given(signService).join(anyString(), anyString(), anyString());
        UserJoinRequest request = UserJoinRequest.builder().email("test@gmail.com").nickname("testUser").job("backend").build();

        //when
        ResultActions result = this.mockMvc.perform(post("/api/auth/join")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("user_join",
                        requestFields(
                                fieldWithPath("email").type(STRING).description("이메일"),
                                fieldWithPath("nickname").type(STRING).description("닉네임"),
                                fieldWithPath("job").type(STRING).description("직군")
                        ),
                        responseFields(
                                fieldWithPath("success").type(BOOLEAN).description("성공 여부"),
                                fieldWithPath("code").type(NUMBER).description("상태 코드"),
                                fieldWithPath("result").type(NULL).description("반환 데이터")
                        )
                ));
    }

    @Test
    @DisplayName("이메일 login : Post /api/auth/login")
    public void email_login() throws Exception {
        //given
        TokenDto response = TokenDto.builder().grantType("Bearer")
                .accessToken("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyIiwicm9sZXMiOlsiUk9MRV9VU0VSIl0sImlhdCI6MTY2MDI4Mzc0NCwiZXhwIjoxNjYwMjg3MzQ0fQ.oE64nKkVmFqRx0LgblAfMXvXDG9lU8sE57DG8heBeAU")
                .refreshToken("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE2NjE0OTMzNDR9.UyY9pJtvBJnLXdlmp0Dk88LvAwFVlKg4-vHirAYxzvM")
                .accessTokenExpireDate(3600000L).build();

        given(signService.login(any(String.class), any(String.class))).willReturn(response);

        //when
        UserLoginRequestDto request = UserLoginRequestDto.builder().email("test@gmail.com").password("1234").build();
        ResultActions result = this.mockMvc.perform(post("/api/auth/login")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("email_login",
                        requestFields(
                                fieldWithPath("email").type(STRING).description("이메일"),
                                fieldWithPath("password").type(STRING).description("비밀번호")
                        ),
                        responseFields(
                                fieldWithPath("success").type(BOOLEAN).description("성공 여부"),
                                fieldWithPath("code").type(NUMBER).description("상태 코드"),
                                fieldWithPath("result.data.grantType").type(STRING).description("Grant Type"),
                                fieldWithPath("result.data.accessToken").type(STRING).description("access token"),
                                fieldWithPath("result.data.refreshToken").type(STRING).description("refresh token"),
                                fieldWithPath("result.data.accessTokenExpireDate").type(NUMBER).description("access token 만료시간")
                        )
                ));
    }

    @Test
    @DisplayName("access token 재발급 : Post /api/auth/reissue")
    public void reissue() throws Exception {
        //given
        TokenDto response = TokenDto.builder().grantType("Bearer")
                .accessToken("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyIiwicm9sZXMiOlsiUk9MRV9VU0VSIl0sImlhdCI6MTY2MDI4Mzc0NCwiZXhwIjoxNjYwMjg3MzQ0fQ.oE64nKkVmFqRx0LgblAfMXvXDG9lU8sE57DG8heBeAU")
                .refreshToken("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE2NjE0OTMzNDR9.UyY9pJtvBJnLXdlmp0Dk88LvAwFVlKg4-vHirAYxzvM")
                .accessTokenExpireDate(3600000L).build();

        given(signService.reissue(anyString(), anyString())).willReturn(response);

        //when
        TokenDto request = TokenDto.builder().grantType("Bearer")
                .accessToken("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIzIiwicm9sZXMiOlsiUk9MRV9VU0VSIl0sImlhdCI6MTY2MDQxMTA5MiwiZXhwIjoxNjYwNDE0NjkyfQ.u1Bz9pEvDy68VJtr233x8E6z3Ua8e5ayhq8TlCc3DeI")
                .refreshToken("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE2NjE2MjA2OTJ9.mG-udQ3mogvgP9ak3rnvU5sW8h3q4sntldz_FUhAIU4")
                .accessTokenExpireDate(3600000L).build();

        ResultActions result = this.mockMvc.perform(post("/api/auth/reissue")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("token_reissue",
                        requestFields(
                                fieldWithPath("grantType").type(STRING).description("Grant Type"),
                                fieldWithPath("accessToken").type(STRING).description("access token"),
                                fieldWithPath("refreshToken").type(STRING).description("refresh token"),
                                fieldWithPath("accessTokenExpireDate").type(NUMBER).description("access token 만료 시간")
                        ),
                        responseFields(
                                fieldWithPath("success").type(BOOLEAN).description("성공 여부"),
                                fieldWithPath("code").type(NUMBER).description("상태 코드"),
                                fieldWithPath("result.data.grantType").type(STRING).description("Grant Type"),
                                fieldWithPath("result.data.accessToken").type(STRING).description("access token"),
                                fieldWithPath("result.data.refreshToken").type(STRING).description("refresh token"),
                                fieldWithPath("result.data.accessTokenExpireDate").type(NUMBER).description("access token 만료시간")
                        )
                ));
    }
}