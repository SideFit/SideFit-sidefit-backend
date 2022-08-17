package com.project.sidefit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.sidefit.domain.entity.Mbti;
import com.project.sidefit.domain.service.UserService;
import com.project.sidefit.domain.service.dto.UserDetailDto;
import com.project.sidefit.domain.service.dto.UserListDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
                .tags(new ArrayList<String>()).build();
        response.add(user);

        given(userService.findAll()).willReturn(response);

        //when
        ResultActions result = this.mockMvc.perform(get("/api/users")
                .accept(APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("get_users",
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
        UserDetailDto user = UserDetailDto.builder().id(1L).mbti(Mbti.INFP)
                .currentStatuses(new ArrayList<String>())
                .favorites(new ArrayList<String>())
                .teches(new ArrayList<String>())
                .tags(new ArrayList<String>())
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
                                fieldWithPath("result.data.tags").type(ARRAY).description("태그")
                        )
                ));
    }
}
