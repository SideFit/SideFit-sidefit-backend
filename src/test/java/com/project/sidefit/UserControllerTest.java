package com.project.sidefit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.sidefit.api.dto.PasswordRequestDto;
import com.project.sidefit.api.dto.PortfolioRequestDto;
import com.project.sidefit.api.dto.UserRequestDto;
import com.project.sidefit.api.dto.sign.EmailRequestDto;
import com.project.sidefit.domain.entity.Mbti;
import com.project.sidefit.domain.service.UserService;
import com.project.sidefit.domain.service.dto.PortfolioDto;
import com.project.sidefit.domain.service.dto.UserDetailDto;
import com.project.sidefit.domain.service.dto.UserDto;
import com.project.sidefit.domain.service.dto.UserListDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.http.MediaType.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
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
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    @WithMockUser
    @DisplayName("전체 회원목록 조회(일부 정보) : Get /api/users")
    public void findAll() throws Exception {
        //given
        List<UserListDto> response = new ArrayList<>();
        UserListDto user = UserListDto.builder().id(1L).nickname("testUser")
                .job("backend").introduction("자기소개 글")
                .tags(Arrays.asList("#포트폴리오용", "#직장인")).build();
        response.add(user);

        /*String accessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyIiwicm9sZXMiOlsiUk9MRV9VU0VSIl0sImlhdCI6MTY2MDI4Mzc0NCwiZXhwIjoxNjYwMjg3MzQ0fQ.oE64nKkVmFqRx0LgblAfMXvXDG9lU8sE57DG8heBeAU";
        String refreshToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE2NjE2MjA2OTJ9.mG-udQ3mogvgP9ak3rnvU5sW8h3q4sntldz_FUhAIU4";

        TokenDto token = TokenDto.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpireDate(60 * 60 * 1000L)
                .build();*/

        given(userService.findAll()).willReturn(response);

        //when
        ResultActions result = this.mockMvc.perform(get("/api/users")
//                .header("X-AUTH-TOKEN", accessToken)
                .accept(APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("get_users",
//                        requestHeaders(
//                                headerWithName("X-AUTH-TOKEN").description("JWT access token")
//                        ),
                        responseFields(
                                fieldWithPath("success").type(BOOLEAN).description("성공 여부"),
                                fieldWithPath("code").type(NUMBER).description("상태 코드"),
                                fieldWithPath("result.data[].id").type(NUMBER).description("user PK"),
                                fieldWithPath("result.data[].nickname").type(STRING).description("닉네임"),
                                fieldWithPath("result.data[].job").type(STRING).description("직군"),
                                fieldWithPath("result.data[].introduction").type(STRING).description("자기소개"),
                                fieldWithPath("result.data[].tags").type(ARRAY).description("태그")
                        )
                ));
    }

    @Test
    @WithMockUser
    @DisplayName("회원 상세조회 : Get /api/user/{id}")
    public void findDetail() throws Exception {
        //given
        PortfolioDto portfolioDto = PortfolioDto.builder().title("깃").url("https://test").build();

        UserDetailDto user = UserDetailDto.builder().id(1L).mbti(Mbti.INFP)
                .currentStatuses(Arrays.asList("구직 중", "포트폴리오 제작 중"))
                .favorites(Arrays.asList("금융", "여행"))
                .teches(Arrays.asList("Java", "Spring"))
                .tags(Arrays.asList("#포트폴리오용", "#직장인"))
                .portfolios(Arrays.asList(portfolioDto))
                .imageUrl("s3://sidefit-bucket/image/09b862cb-1c00-4d87-a8e8-dc7ceb4e7761.png")
                .build();

        given(userService.findDetail(anyLong())).willReturn(user);

        //when
        ResultActions result = this.mockMvc.perform(RestDocumentationRequestBuilders.get("/api/user/{id}", 1L)
                .accept(APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("get_user_detail",
                        pathParameters(
                                parameterWithName("id").description("user PK")
                        ),
                        responseFields(
                                fieldWithPath("success").type(BOOLEAN).description("성공 여부"),
                                fieldWithPath("code").type(NUMBER).description("상태 코드"),
                                fieldWithPath("result.data.id").type(NUMBER).description("user PK"),
                                fieldWithPath("result.data.mbti").type(STRING).description("MBTI"),
                                fieldWithPath("result.data.currentStatuses").type(ARRAY).description("현재 상태"),
                                fieldWithPath("result.data.favorites").type(ARRAY).description("좋아하는 분야"),
                                fieldWithPath("result.data.teches").type(ARRAY).description("기술 스택"),
                                fieldWithPath("result.data.tags").type(ARRAY).description("태그"),
                                fieldWithPath("result.data.portfolios[].title").type(STRING).description("포트폴리오 이름"),
                                fieldWithPath("result.data.portfolios[].url").type(STRING).description("포트폴리오 링크"),
                                fieldWithPath("result.data.imageUrl").type(STRING).description("이미지 url")
                        )
                ));
    }

    @Test
    @DisplayName("비밀번호 변경을 위한 링크를 메일에 전송 : Post /api/user/password/email")
    public void sendPasswordEmail() throws Exception {
        //given
        EmailRequestDto request = EmailRequestDto.builder().email("test@gmail.com").build();
        willDoNothing().given(userService).sendPasswordEmail(anyString());

        //when
        ResultActions result = this.mockMvc.perform(post("/api/user/password/email")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("send_password_email",
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
    @DisplayName("비밀번호 변경처리 : Patch /api/user/password/{token}")
    public void updatePassword() throws Exception {
        //given
        PasswordRequestDto request = PasswordRequestDto.builder().password("test123password").passwordCheck("test123password").build();
        willDoNothing().given(userService).updatePassword(anyString(), anyString());

        //when
        ResultActions result = this.mockMvc.perform(RestDocumentationRequestBuilders.patch("/api/user/password/{token}", "5b52fe61-6a3f-4910-9cb8-186e0ac65053")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("change_password",
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

//    @Test
//    @WithMockUser
//    @DisplayName("프로필 변경 : Patch /api/user/{id}")
//    public void 프로필_변경() throws Exception {
//
//        //given
//        PortfolioRequestDto portfolioDto = PortfolioRequestDto.builder().title("깃").url("https://test").build();
//
//        MockMultipartFile image = new MockMultipartFile("image", "image.png", "image/png", "<<png data>>".getBytes());
//        MockMultipartFile metadata = new MockMultipartFile("metadata", "", "application/json",
//                "{ \"version\": \"1.0\"}".getBytes());
//
//
////        multipart("/upload").file("file", "example".getBytes())
//
//
//        UserRequestDto userRequestDto = UserRequestDto.builder().job("frontend").introduction("hi~test")
//                .tags(Arrays.asList("#포트폴리오용", "#직장인"))
//                .currentStatuses(Arrays.asList("구직 중", "포트폴리오 제작 중"))
//                .favorites(Arrays.asList("금융", "여행"))
//                .teches(Arrays.asList("Java", "Spring"))
//                .portfolios(Arrays.asList(portfolioDto))
//                .mbti(Mbti.ENFP).build();
//
//        UserDto userDto = userRequestDto.toUserDto();
//        willDoNothing().given(userService).updateUser(anyLong(), eq(userDto));
//
//        //when
//        ResultActions result = this.mockMvc.perform(RestDocumentationRequestBuilders.patch("/api/user/{id}", 1L)
//                .contentType(APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(userRequestDto))
//                .accept(APPLICATION_JSON)
//        );
//
//        //then
//        result.andExpect(status().isOk())
//                .andDo(document("update_user",
//                        pathParameters(
//                                parameterWithName("id").description("user PK")
//                        ),
//                        requestFields(
//                                fieldWithPath("job").type(STRING).description("직군"),
//                                fieldWithPath("introduction").type(STRING).description("자기 소개"),
//                                fieldWithPath("tags").type(ARRAY).description("태그"),
//                                fieldWithPath("currentStatuses").type(ARRAY).description("현재 상태"),
//                                fieldWithPath("favorites").type(ARRAY).description("관심 분야"),
//                                fieldWithPath("teches").type(ARRAY).description("기술 스택"),
//                                fieldWithPath("mbti").type(STRING).description("mbti")
//                        ),
//                        responseFields(
//                                fieldWithPath("success").type(BOOLEAN).description("성공 여부"),
//                                fieldWithPath("code").type(NUMBER).description("상태 코드"),
//                                fieldWithPath("result").type(NULL).description("반환 데이터")
//                        )
//                ));
//    }
}
