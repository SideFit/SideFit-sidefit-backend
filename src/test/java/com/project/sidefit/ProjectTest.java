package com.project.sidefit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.sidefit.domain.entity.*;
import com.project.sidefit.domain.entity.user.User;
import com.project.sidefit.domain.repository.ImageRepository;
import com.project.sidefit.domain.repository.UserRepository;
import com.project.sidefit.domain.repository.project.KeywordRepository;
import com.project.sidefit.domain.repository.project.ProjectRepository;
import com.project.sidefit.domain.repository.project.ProjectUserRepository;
import com.project.sidefit.domain.service.ProjectService;
import com.project.sidefit.domain.service.dto.TokenDto;
import com.project.sidefit.domain.service.auth.AuthService;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.annotation.DirtiesContext;
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ProjectTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private AuthService signService;

    @Autowired
    private UserRepository userRepository;

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

    @BeforeEach
    void beforeEach() {
        User leader = User.createUser("leader@gmail.com", encoder.encode("pw"), "leader", "leader");
        User user1 = User.createUser("user1@gmail.com", encoder.encode("pw1"), "user1", "user");
        User user2 = User.createUser("user2@gmail.com", encoder.encode("pw2"), "user2", "user");
        User user3 = User.createUser("user3@gmail.com", encoder.encode("pw3"), "user3", "user");
        userRepository.save(leader);
        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

        Image image = new Image("test-image", "image-url");
        imageRepository.save(image);

        RecruitRequestDto recruitDto1 = new RecruitRequestDto("백엔드", 3);
        RecruitRequestDto recruitDto2 = new RecruitRequestDto("프론트엔드", 2);
        RecruitRequestDto recruitDto3 = new RecruitRequestDto("백엔드", 1);
        RecruitRequestDto recruitDto4 = new RecruitRequestDto("프론트엔드", 2);

        ProjectRequestDto projectRequestDto1 = new ProjectRequestDto("test1", 1, "스포츠", "This is test project", "1달", "#Java#Spring", "plan1", "#hashtag1", image.getName(), image.getImageUrl(), List.of(recruitDto1, recruitDto2));
        ProjectRequestDto projectRequestDto2 = new ProjectRequestDto("test2", 2, "웰빙", "This is test project", "2달", "#JavaScript#NodeJs", "plan2", "#hashtag2", image.getName(), image.getImageUrl(), List.of(recruitDto3, recruitDto4));
        projectService.makeProject(leader.getId(), image.getId(), projectRequestDto1);
        projectService.makeProject(leader.getId(), image.getId(), projectRequestDto2);
    }

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
        TokenDto token = signService.login("leader@gmail.com", "pw");
        Image image = imageRepository.getReferenceById(1L);

        RecruitRequestDto recruitRequestDto1 = new RecruitRequestDto("프론트엔드", 3);
        RecruitRequestDto recruitRequestDto2 = new RecruitRequestDto("백엔드", 2);
        RecruitRequestDto recruitRequestDto3 = new RecruitRequestDto("디자이너", 1);
        List<RecruitRequestDto> recruitRequestDtoList = Arrays.asList(recruitRequestDto1, recruitRequestDto2, recruitRequestDto3);

        ProjectRequestDto projectRequestDto = new ProjectRequestDto("test", 0, "#웰빙", "hi!", "1 month", "#Java#Spring", "plan", "#hashtag", image.getName(), null, recruitRequestDtoList);

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
                                fieldWithPath("result.data").type(NUMBER).description("프로젝트 id")
                        )
                ));
    }

    @Test
    @WithMockUser
    @DisplayName("PATCH /api/project")
    void update_project_test() throws Exception {
        //given
        TokenDto token = signService.login("leader@gmail.com", "pw");
        Image image = imageRepository.getReferenceById(1L);
        Project project = projectRepository.getReferenceById(1L);

        RecruitRequestDto recruitRequestDto1 = new RecruitRequestDto("프론트엔드", 3);
        RecruitRequestDto recruitRequestDto2 = new RecruitRequestDto("백엔드", 2);
        RecruitRequestDto recruitRequestDto3 = new RecruitRequestDto("디자이너", 1);
        List<RecruitRequestDto> recruitRequestDtoList = Arrays.asList(recruitRequestDto1, recruitRequestDto2, recruitRequestDto3);

        ProjectRequestDto projectRequestDto = new ProjectRequestDto("update", 2, "#게임", "hi!!!", "3 month", "#Java", "new-plan", "#update", image.getName(), null, recruitRequestDtoList);

        //when
        ResultActions result = mockMvc.perform(patch("/api/project")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-AUTH-TOKEN", token.getAccessToken())
                .param("projectId", String.valueOf(project.getId()))
                .param("imageId", String.valueOf(image.getId()))
                .content(objectMapper.writeValueAsString(projectRequestDto))
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("update_project",
                        requestParameters(
                                parameterWithName("projectId").description("프로젝트 id"),
                                parameterWithName("imageId").description("이미지 id")
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
        TokenDto token = signService.login("leader@gmail.com", "pw");
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
    @DisplayName("DELETE /api/project")
    void delete_project_test() throws Exception {
        //given
        TokenDto token = signService.login("leader@gmail.com", "pw");
        Project project = projectRepository.getReferenceById(1L);

        //when
        ResultActions result = mockMvc.perform(delete("/api/project")
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
        User user1 = userRepository.getReferenceById(2L);
        User user2 = userRepository.getReferenceById(3L);
        User user3 = userRepository.getReferenceById(4L);

        Image image = imageRepository.getReferenceById(1L);
        user1.updateImage(image);
        user2.updateImage(image);
        user3.updateImage(image);

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
        User user1 = userRepository.getReferenceById(2L);
        User user2 = userRepository.getReferenceById(3L);
        User user3 = userRepository.getReferenceById(4L);

        Image image = imageRepository.getReferenceById(1L);
        user1.updateImage(image);
        user2.updateImage(image);
        user3.updateImage(image);

        TokenDto token = signService.login("leader@gmail.com", "pw");

        Project project1 = projectRepository.getReferenceById(1L);
        Project project2 = projectRepository.getReferenceById(2L);

        ProjectUser projectUser1 = ProjectUser.createProjectUser(user1, project1);
        ProjectUser projectUser2 = ProjectUser.createProjectUser(user2, project1);
        ProjectUser projectUser3 = ProjectUser.createProjectUser(user3, project2);
        projectUserRepository.save(projectUser1);
        projectUserRepository.save(projectUser2);
        projectUserRepository.save(projectUser3);

        projectService.endProject(project1.getId()); // 프로젝트 1 종료 처리 -> 이전 멤버로 조회 가능

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
        TokenDto token = signService.login("user1@gmail.com", "pw1");

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
        //when
        ResultActions result = mockMvc.perform(get("/api/project/search")
                .contentType(MediaType.APPLICATION_JSON)
                .param("keyword", "test")
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("get_project_search",
                        requestParameters(
                                parameterWithName("keyword").description("검색 키워드")
                        ),
                        responseFields(
                                fieldWithPath("success").type(BOOLEAN).description("성공 여부"),
                                fieldWithPath("code").type(NUMBER).description("상태 코드"),
                                fieldWithPath("result.data[].id").type(NUMBER).description("프로젝트 id"),
                                fieldWithPath("result.data[].title").type(STRING).description("프로젝트 제목"),
                                fieldWithPath("result.data[].type").type(NUMBER).description("프로젝트 유형"),
                                fieldWithPath("result.data[].field").type(STRING).description("프로젝트 분야"),
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
    @DisplayName("GET /api/project/keyword-search")
    void keyword_search_project_test() throws Exception {
        // given
        KeywordSearchRequestDto keywordSearchRequestDto = new KeywordSearchRequestDto(List.of("백엔드", "프론트엔드"), List.of("웰빙", "스포츠"), List.of("1달", "2달"), List.of(1, 2));

        //when
        ResultActions result = mockMvc.perform(post("/api/project/keyword-search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(keywordSearchRequestDto))
                .param("condition", "")
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("get_keyword_project_search",
                        requestFields(
                                fieldWithPath("jobGroups").type(ARRAY).description("직군 키워드"),
                                fieldWithPath("fields").type(ARRAY).description("분야 키워드"),
                                fieldWithPath("periods").type(ARRAY).description("프로젝트 기간 키워드"),
                                fieldWithPath("types").type(ARRAY).description("프로젝트 유형 키워드")
                        ),
                        requestParameters(
                                parameterWithName("condition").description("정렬 기준")
                        ),
                        responseFields(
                                fieldWithPath("success").type(BOOLEAN).description("성공 여부"),
                                fieldWithPath("code").type(NUMBER).description("상태 코드"),
                                fieldWithPath("result.data[].id").type(NUMBER).description("프로젝트 id"),
                                fieldWithPath("result.data[].title").type(STRING).description("프로젝트 제목"),
                                fieldWithPath("result.data[].type").type(NUMBER).description("프로젝트 유형"),
                                fieldWithPath("result.data[].field").type(STRING).description("프로젝트 분야"),
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

    private Project createProject(User leader, Image image, String title, int type, String field, String introduction, String period, String stack, String meetingPlan, String hashtag, boolean status) {
        return Project.builder()
                .user(leader)
                .image(image)
                .title(title)
                .type(type)
                .field(field)
                .introduction(introduction)
                .period(period)
                .stack(stack)
                .meetingPlan(meetingPlan)
                .hashtag(hashtag)
                .status(status)
                .build();
    }
}
