package com.project.sidefit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.sidefit.domain.entity.*;
import com.project.sidefit.domain.enums.SearchCondition;
import com.project.sidefit.domain.repository.ImageRepository;
import com.project.sidefit.domain.repository.UserJpaRepo;
import com.project.sidefit.domain.repository.project.KeywordRepository;
import com.project.sidefit.domain.repository.project.ProjectRepository;
import com.project.sidefit.domain.repository.project.ProjectUserRepository;
import com.project.sidefit.domain.service.ApplyService;
import com.project.sidefit.domain.service.ProjectService;
import com.project.sidefit.domain.service.dto.TokenDto;
import com.project.sidefit.domain.service.security.SignService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.project.sidefit.api.dto.ProjectDto.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
@SpringBootTest
public class ProjectTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private SignService signService;

    @Autowired
    private ApplyService applyService;

    @Autowired
    private UserJpaRepo userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectUserRepository projectUserRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private KeywordRepository keywordRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Test
    @WithMockUser
    @DisplayName("GET /api/project/{projectId}")
    void get_project_test() throws Exception {
        //given
        Project project = projectRepository.getReferenceById(1L);

        //when
        ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.get("/api/project/{projectId}", String.valueOf(project.getId()))
                .contentType(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("get_project",
                        pathParameters(
                                parameterWithName("projectId").description("프로젝트 id")
                        ),
                        responseFields(
                                fieldWithPath("success").type(BOOLEAN).description("성공 여부"),
                                fieldWithPath("code").type(NUMBER).description("상태 코드"),
                                fieldWithPath("result.data.id").type(NUMBER).description("프로젝트 id"),
                                fieldWithPath("result.data.userId").type(NUMBER).description("팀장 id"),
                                fieldWithPath("result.data.imageId").type(NUMBER).description("이미지 id"),
                                fieldWithPath("result.data.title").type(STRING).description("프로젝트 제목"),
                                fieldWithPath("result.data.type").type(NUMBER).description("프로젝트 타입"),
                                fieldWithPath("result.data.introduction").type(STRING).description("프로젝트 소개"),
                                fieldWithPath("result.data.period").type(STRING).description("프로젝트 기간"),
                                fieldWithPath("result.data.stack").type(STRING).description("필요 스택"),
                                fieldWithPath("result.data.meetingPlan").type(STRING).description("모임 계획"),
                                fieldWithPath("result.data.hashtag").type(STRING).description("해시 태그"),
                                fieldWithPath("result.data.status").type(BOOLEAN).description("진행 상태"),
                                fieldWithPath("result.data.recruits[].id").type(NUMBER).description("모집 id"),
                                fieldWithPath("result.data.recruits[].projectId").type(NUMBER).description("프로젝트 id"),
                                fieldWithPath("result.data.recruits[].jobGroup").type(STRING).description("직군"),
                                fieldWithPath("result.data.recruits[].currentNumber").type(NUMBER).description("현재 인원"),
                                fieldWithPath("result.data.recruits[].recruitNumber").type(NUMBER).description("모집 인원"),
                                fieldWithPath("result.data.createdDate").type(STRING).description("생성 일자"),
                                fieldWithPath("result.data.lastModifiedDate").type(STRING).description("수정 일자")
                        )
                ));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/project")
    void create_project_test() throws Exception {
        //given
        User teamLeader = userRepository.getReferenceById(1L);
        TokenDto token = signService.login(teamLeader.getEmail(), "pw1");
        Image image = imageRepository.getReferenceById(1L);

        RecruitRequestDto recruitRequestDto1 = new RecruitRequestDto("프론트엔드", 3);
        RecruitRequestDto recruitRequestDto2 = new RecruitRequestDto("백엔드", 2);
        RecruitRequestDto recruitRequestDto3 = new RecruitRequestDto("디자이너", 1);
        List<RecruitRequestDto> recruitRequestDtoList = Arrays.asList(recruitRequestDto1, recruitRequestDto2, recruitRequestDto3);

        ProjectRequestDto projectRequestDto = new ProjectRequestDto("test", 0, "#웰빙", "hi!", "1 month", "#Java#Spring", "plan", "#hashtag", image.getName(), image.getImageUrl(), recruitRequestDtoList);

        //when
        ResultActions result = mockMvc.perform(post("/api/project")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-AUTH-TOKEN", token.getAccessToken())
                .param("imageId", String.valueOf(image.getId()))
                .content(objectMapper.writeValueAsString(projectRequestDto))
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("create_project",
                        requestParameters(
                                parameterWithName("imageId").description("이미지 id")
                        ),
                        requestFields(
                                fieldWithPath("title").description("프로젝트 제목"),
                                fieldWithPath("type").description("프로젝트 유형"),
                                fieldWithPath("field").description("프로젝트 분야"),
                                fieldWithPath("introduction").description("소개글"),
                                fieldWithPath("period").description("프로젝트 기간"),
                                fieldWithPath("stack").description("기술 스택"),
                                fieldWithPath("meetingPlan").description("모임 계획"),
                                fieldWithPath("hashtag").description("해시 태그"),
                                fieldWithPath("name").description("이미지 이름"),
                                fieldWithPath("imageUrl").description("이미지 url"),
                                fieldWithPath("recruits[].jobGroup").description("직군"),
                                fieldWithPath("recruits[].recruitNumber").description("모집 인원")
                        ),
                        responseFields(
                                fieldWithPath("success").type(BOOLEAN).description("성공 여부"),
                                fieldWithPath("code").type(NUMBER).description("상태 코드"),
                                fieldWithPath("result").type(NULL).description("결과 메시지")
                        )
                ));
    }

    @Test
    @WithMockUser
    @DisplayName("PATCH /api/project/end")
    void end_project_test() throws Exception {
        //given
        TokenDto token = signService.login("email5@gmail.com", "pw5");
        Project project = projectRepository.getReferenceById(1L);

        //when
        ResultActions result = mockMvc.perform(patch("/api/project/end")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-AUTH-TOKEN", token.getAccessToken())
                .param("projectId", String.valueOf(project.getId()))
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("end_project",
                        requestParameters(
                                parameterWithName("projectId").description("프로젝트 id")
                        ),
                        responseFields(
                                fieldWithPath("success").type(BOOLEAN).description("성공 여부"),
                                fieldWithPath("code").type(NUMBER).description("상태 코드"),
                                fieldWithPath("result").type(NULL).description("결과 메시지")
                        )
                ));
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/project/delete")
    void delete_project_test() throws Exception {
        //given
        TokenDto token = signService.login("email5@gmail.com", "pw5");
        Image image = imageRepository.getReferenceById(1L);
        Project project = projectRepository.getReferenceById(1L);

        //when
        ResultActions result = mockMvc.perform(delete("/api/project/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-AUTH-TOKEN", token.getAccessToken())
                .param("projectId", String.valueOf(project.getId()))
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("delete_project",
                        requestParameters(
                                parameterWithName("projectId").description("프로젝트 id")
                        ),
                        responseFields(
                                fieldWithPath("success").type(BOOLEAN).description("성공 여부"),
                                fieldWithPath("code").type(NUMBER).description("상태 코드"),
                                fieldWithPath("result").type(NULL).description("결과 메시지")
                        )
                ));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/project/{projectId}/member/list")
    void project_member_list_test() throws Exception {
        //given
        User user1 = userRepository.getReferenceById(1L);
        User user2 = userRepository.getReferenceById(2L);
        User user3 = userRepository.getReferenceById(3L);

        Project project = projectRepository.getReferenceById(1L);

        ProjectUser projectUser1 = ProjectUser.createProjectUser(user1, project);
        ProjectUser projectUser2 = ProjectUser.createProjectUser(user2, project);
        ProjectUser projectUser3 = ProjectUser.createProjectUser(user3, project);
        projectUserRepository.save(projectUser1);
        projectUserRepository.save(projectUser2);
        projectUserRepository.save(projectUser3);

        //when
        ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.get("/api/project/{projectId}/member/list", String.valueOf(project.getId())));

        //then
        result.andExpect(status().isOk())
                .andDo(document("get_project_members",
                        pathParameters(
                                parameterWithName("projectId").description("프로젝트 id")
                        ),
                        responseFields(
                                fieldWithPath("success").type(BOOLEAN).description("성공 여부"),
                                fieldWithPath("code").type(NUMBER).description("상태 코드"),
                                fieldWithPath("result.data[].id").type(NUMBER).description("회원 id"),
                                fieldWithPath("result.data[].nickname").type(STRING).description("회원 닉네임"),
                                fieldWithPath("result.data[].job").type(STRING).description("회원 직무"),
                                fieldWithPath("result.data[].imageId").type(NUMBER).description("회원 이미지 id"),
                                fieldWithPath("result.data[].imageUrl").type(STRING).description("회원 이미지 url")
                        )
                ));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/project/pre-member/list")
    void project_pre_member_list_test() throws Exception {
        //given
        User user1 = userRepository.getReferenceById(1L);
        User user2 = userRepository.getReferenceById(2L);
        User user3 = userRepository.getReferenceById(3L);
        User user4 = userRepository.getReferenceById(4L);

        TokenDto token = signService.login("email5@gmail.com", "pw5");

        Project project1 = projectRepository.getReferenceById(1L);
        Project project2 = projectRepository.getReferenceById(2L);

        ProjectUser projectUser1 = ProjectUser.createProjectUser(user1, project1);
        ProjectUser projectUser2 = ProjectUser.createProjectUser(user2, project1);
        ProjectUser projectUser3 = ProjectUser.createProjectUser(user3, project2);
        ProjectUser projectUser4 = ProjectUser.createProjectUser(user4, project2);
        projectUserRepository.save(projectUser1);
        projectUserRepository.save(projectUser2);
        projectUserRepository.save(projectUser3);
        projectUserRepository.save(projectUser4);

        projectService.endProject(project1.getId()); // 프로젝트 1 종료 처리

        //when
        ResultActions result = mockMvc.perform(get("/api/project/pre-member/list")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-AUTH-TOKEN", token.getAccessToken())
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("get_project_pre_members",
                        responseFields(
                                fieldWithPath("success").type(BOOLEAN).description("성공 여부"),
                                fieldWithPath("code").type(NUMBER).description("상태 코드"),
                                fieldWithPath("result.data[].id").type(NUMBER).description("회원 id"),
                                fieldWithPath("result.data[].nickname").type(STRING).description("회원 닉네임"),
                                fieldWithPath("result.data[].job").type(STRING).description("회원 직무"),
                                fieldWithPath("result.data[].imageId").type(NUMBER).description("회원 이미지 id"),
                                fieldWithPath("result.data[].imageUrl").type(STRING).description("회원 이미지 url")
                        )
                ));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/project/recommend/list")
    void recommend_projects_test() throws Exception {
        //given
        User user = userRepository.getReferenceById(1L);
        TokenDto token = signService.login(user.getEmail(), "pw1");

        //when
        ResultActions result = mockMvc.perform(get("/api/project/recommend/list")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-AUTH-TOKEN", token.getAccessToken())
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("get_project_recommend_list",
                        responseFields(
                                fieldWithPath("success").type(BOOLEAN).description("성공 여부"),
                                fieldWithPath("code").type(NUMBER).description("상태 코드"),
                                fieldWithPath("result.data[].id").type(NUMBER).description("프로젝트 id"),
                                fieldWithPath("result.data[].imageId").type(NUMBER).description("이미지 id"),
                                fieldWithPath("result.data[].title").type(STRING).description("프로젝트 제목"),
                                fieldWithPath("result.data[].type").type(NUMBER).description("프로젝트 유형"),
                                fieldWithPath("result.data[].hashtag").type(STRING).description("프로젝트 해시태그"),
                                fieldWithPath("result.data[].status").type(BOOLEAN).description("프로젝트 진행 상태"),
                                fieldWithPath("result.data[].createdDate").type(STRING).description("생성 일자"),
                                fieldWithPath("result.data[].lastModifiedDate").type(STRING).description("수정 일자")
                        )
                ));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/project/search")
    void search_project_test() throws Exception {
        //given
        SearchRequestDto searchRequestDto = new SearchRequestDto("test", SearchCondition.LATEST_ORDER);

        //when
        ResultActions result = mockMvc.perform(get("/api/project/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchRequestDto))
                .accept(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("get_project_search",
                        requestFields(
                                fieldWithPath("keyword").type(STRING).description("검색 키워드"),
                                fieldWithPath("condition").type(STRING).description("검색 조건")
                        ),
                        responseFields(
                                fieldWithPath("success").type(BOOLEAN).description("성공 여부"),
                                fieldWithPath("code").type(NUMBER).description("상태 코드"),
                                fieldWithPath("result.data[].id").type(NUMBER).description("프로젝트 id"),
                                fieldWithPath("result.data[].title").type(STRING).description("프로젝트 제목"),
                                fieldWithPath("result.data[].type").type(NUMBER).description("프로젝트 유형"),
                                fieldWithPath("result.data[].hashtag").type(STRING).description("프로젝트 해시태그"),
                                fieldWithPath("result.data[].status").type(BOOLEAN).description("프로젝트 진행 상태"),
                                fieldWithPath("result.data[].createdDate").type(STRING).description("생성 일자"),
                                fieldWithPath("result.data[].lastModifiedDate").type(STRING).description("수정 일자"),
                                fieldWithPath("result.data[].imageId").type(NUMBER).description("이미지 id"),
                                fieldWithPath("result.data[].imageUrl").type(STRING).description("이미지 url"),
                                fieldWithPath("result.data[].recruits[].id").type(NUMBER).description("모집 id"),
                                fieldWithPath("result.data[].recruits[].projectId").type(NUMBER).description("프로젝트 id"),
                                fieldWithPath("result.data[].recruits[].jobGroup").type(STRING).description("모집 직군"),
                                fieldWithPath("result.data[].recruits[].currentNumber").type(NUMBER).description("현재 인원"),
                                fieldWithPath("result.data[].recruits[].recruitNumber").type(NUMBER).description("모집 인원")
                        )
                ));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/project/search/recommend/list")
    void recommend_keyword_test() throws Exception {
        //when
        ResultActions result = mockMvc.perform(get("/api/project/search/recommend/list").contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isOk())
                .andDo(document("get_project_search_recommend_list",
                        responseFields(
                                fieldWithPath("success").type(BOOLEAN).description("성공 여부"),
                                fieldWithPath("code").type(NUMBER).description("상태 코드"),
                                fieldWithPath("result.data[].id").type(NUMBER).description("키워드 id"),
                                fieldWithPath("result.data[].word").type(STRING).description("키워드")
                        )
                ));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/project/search/recommend/{keywordId}")
    void select_recommend_keyword_test() throws Exception {
        //given
        Keyword keyword = keywordRepository.getReferenceById(1L);

        //when
        ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.get("/api/project/search/recommend/{keywordId}", keyword.getId())
                .contentType(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("get_project_search_recommend_keyword",
                        pathParameters(
                                parameterWithName("keywordId").description("키워드 id")
                        ),
                        responseFields(
                                fieldWithPath("success").type(BOOLEAN).description("성공 여부"),
                                fieldWithPath("code").type(NUMBER).description("상태 코드"),
                                fieldWithPath("result.data[].id").type(NUMBER).description("프로젝트 id"),
                                fieldWithPath("result.data[].title").type(STRING).description("프로젝트 제목"),
                                fieldWithPath("result.data[].type").type(NUMBER).description("프로젝트 유형"),
                                fieldWithPath("result.data[].hashtag").type(STRING).description("프로젝트 해시태그"),
                                fieldWithPath("result.data[].status").type(BOOLEAN).description("프로젝트 진행 상태"),
                                fieldWithPath("result.data[].createdDate").type(STRING).description("생성 일자"),
                                fieldWithPath("result.data[].lastModifiedDate").type(STRING).description("수정 일자"),
                                fieldWithPath("result.data[].imageId").type(NUMBER).description("이미지 id"),
                                fieldWithPath("result.data[].imageUrl").type(STRING).description("이미지 url"),
                                fieldWithPath("result.data[].recruits[].id").type(NUMBER).description("모집 id"),
                                fieldWithPath("result.data[].recruits[].projectId").type(NUMBER).description("프로젝트 id"),
                                fieldWithPath("result.data[].recruits[].jobGroup").type(STRING).description("모집 직군"),
                                fieldWithPath("result.data[].recruits[].currentNumber").type(NUMBER).description("현재 인원"),
                                fieldWithPath("result.data[].recruits[].recruitNumber").type(NUMBER).description("모집 인원")
                        )
                ));
    }
}
