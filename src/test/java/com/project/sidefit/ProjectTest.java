package com.project.sidefit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.sidefit.domain.entity.Image;
import com.project.sidefit.domain.entity.ProjectUser;
import com.project.sidefit.domain.entity.User;
import com.project.sidefit.domain.repository.ImageRepository;
import com.project.sidefit.domain.repository.UserJpaRepo;
import com.project.sidefit.domain.repository.project.ProjectRepository;
import com.project.sidefit.domain.repository.project.ProjectUserRepository;
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
    private UserJpaRepo userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectUserRepository projectUserRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Test
    @WithMockUser
    @DisplayName("GET /api/project/{projectId}")
    void get_project_test() throws Exception {
        //given
        User teamLeader = User.createUser("test@gmail.com", encoder.encode("pw"), "tester", "job");
        userRepository.save(teamLeader);

        TokenDto token = signService.login("test@gmail.com", "pw");

        Image image = new Image("default-image", "url");
        imageRepository.save(image);

        RecruitRequestDto recruitRequestDto1 = new RecruitRequestDto("프론트엔드", 1);
        RecruitRequestDto recruitRequestDto2 = new RecruitRequestDto("백엔드", 2);
        RecruitRequestDto recruitRequestDto3 = new RecruitRequestDto("디자이너", 3);
        List<RecruitRequestDto> recruitRequestDtoList = Arrays.asList(recruitRequestDto1, recruitRequestDto2, recruitRequestDto3);

        ProjectRequestDto projectRequestDto = new ProjectRequestDto("test", 0, "#웰빙", "hi!", "1 month", "#Java#Spring", "plan", "#hashtag", image.getName(), image.getImageUrl(), recruitRequestDtoList);
        Long projectId = projectService.makeProject(teamLeader.getId(), image.getId(), projectRequestDto);

        //when
        ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.get("/api/project/{projectId}", String.valueOf(projectId))
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-AUTH-TOKEN", token.getAccessToken())
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
        User teamLeader = User.createUser("test@gmail.com", encoder.encode("pw"), "tester", "job");
        userRepository.save(teamLeader);

        TokenDto token = signService.login("test@gmail.com", "pw");

        Image image = new Image("default-image", "url");
        imageRepository.save(image);

        RecruitRequestDto recruitRequestDto1 = new RecruitRequestDto("프론트엔드", 1);
        RecruitRequestDto recruitRequestDto2 = new RecruitRequestDto("백엔드", 2);
        RecruitRequestDto recruitRequestDto3 = new RecruitRequestDto("디자이너", 3);
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
        User teamLeader = User.createUser("test@gmail.com", encoder.encode("pw"), "tester", "job");
        userRepository.save(teamLeader);

        TokenDto token = signService.login("test@gmail.com", "pw");

        Image image = new Image("default-image", "url");
        imageRepository.save(image);

        RecruitRequestDto recruitRequestDto1 = new RecruitRequestDto("프론트엔드", 1);
        RecruitRequestDto recruitRequestDto2 = new RecruitRequestDto("백엔드", 2);
        RecruitRequestDto recruitRequestDto3 = new RecruitRequestDto("디자이너", 3);
        List<RecruitRequestDto> recruitRequestDtoList = Arrays.asList(recruitRequestDto1, recruitRequestDto2, recruitRequestDto3);

        ProjectRequestDto projectRequestDto = new ProjectRequestDto("test", 0, "#웰빙", "hi!", "1 month", "#Java#Spring", "plan", "#hashtag", image.getName(), image.getImageUrl(), recruitRequestDtoList);
        Long projectId = projectService.makeProject(teamLeader.getId(), image.getId(), projectRequestDto);

        //when
        ResultActions result = mockMvc.perform(patch("/api/project/end")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-AUTH-TOKEN", token.getAccessToken())
                .param("projectId", String.valueOf(projectId))
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
        User teamLeader = User.createUser("test@gmail.com", encoder.encode("pw"), "tester", "job");
        userRepository.save(teamLeader);

        TokenDto token = signService.login("test@gmail.com", "pw");

        Image image = new Image("default-image", "url");
        imageRepository.save(image);

        RecruitRequestDto recruitRequestDto1 = new RecruitRequestDto("프론트엔드", 1);
        RecruitRequestDto recruitRequestDto2 = new RecruitRequestDto("백엔드", 2);
        RecruitRequestDto recruitRequestDto3 = new RecruitRequestDto("디자이너", 3);
        List<RecruitRequestDto> recruitRequestDtoList = Arrays.asList(recruitRequestDto1, recruitRequestDto2, recruitRequestDto3);

        ProjectRequestDto projectRequestDto = new ProjectRequestDto("test", 0, "#웰빙", "hi!", "1 month", "#Java#Spring", "plan", "#hashtag", image.getName(), image.getImageUrl(), recruitRequestDtoList);
        Long projectId = projectService.makeProject(teamLeader.getId(), image.getId(), projectRequestDto);

        //when
        ResultActions result = mockMvc.perform(delete("/api/project/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-AUTH-TOKEN", token.getAccessToken())
                .param("projectId", String.valueOf(projectId))
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
        Image image = new Image("default-image", "url");
        imageRepository.save(image);

        User teamLeader = User.createUser("leader@gmail.com", encoder.encode("pw"), "tester1", "job");
        User member1 = User.createUser("m1@gmail.com", encoder.encode("pw"), "tester2", "job");
        User member2 = User.createUser("m2@gmail.com", encoder.encode("pw"), "tester3", "job");
        User member3 = User.createUser("m3@gmail.com", encoder.encode("pw"), "tester4", "job");
        teamLeader.updateImage(image);
        member1.updateImage(image);
        member2.updateImage(image);
        member3.updateImage(image);
        userRepository.save(teamLeader);
        userRepository.save(member1);
        userRepository.save(member2);
        userRepository.save(member3);

        RecruitRequestDto recruitRequestDto1 = new RecruitRequestDto("프론트엔드", 1);
        RecruitRequestDto recruitRequestDto2 = new RecruitRequestDto("백엔드", 2);
        RecruitRequestDto recruitRequestDto3 = new RecruitRequestDto("디자이너", 3);
        List<RecruitRequestDto> recruitRequestDtoList = Arrays.asList(recruitRequestDto1, recruitRequestDto2, recruitRequestDto3);

        ProjectRequestDto projectRequestDto = new ProjectRequestDto("test", 0, "#웰빙", "hi!", "1 month", "#Java#Spring", "plan", "#hashtag", image.getName(), image.getImageUrl(), recruitRequestDtoList);
        Long projectId = projectService.makeProject(teamLeader.getId(), image.getId(), projectRequestDto);

        //when
        ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.get("/api/project/{projectId}/member/list", String.valueOf(projectId)));

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
        Image image = new Image("default-image", "url");
        imageRepository.save(image);

        User teamLeader = User.createUser("leader@gmail.com", encoder.encode("pw"), "leader", "job");
        User member1 = User.createUser("m1@gmail.com", encoder.encode("pw"), "tester1", "job");
        User member2 = User.createUser("m2@gmail.com", encoder.encode("pw"), "tester2", "job");
        User member3 = User.createUser("m3@gmail.com", encoder.encode("pw"), "tester3", "job");
        User member4 = User.createUser("m4@gmail.com", encoder.encode("pw"), "tester4", "job");
        User member5 = User.createUser("m5@gmail.com", encoder.encode("pw"), "tester5", "job");
        teamLeader.updateImage(image);
        member1.updateImage(image);
        member2.updateImage(image);
        member3.updateImage(image);
        member4.updateImage(image);
        member5.updateImage(image);
        userRepository.save(teamLeader);
        userRepository.save(member1);
        userRepository.save(member2);
        userRepository.save(member3);
        userRepository.save(member4);
        userRepository.save(member5);

        TokenDto token = signService.login("leader@gmail.com", "pw");

        RecruitRequestDto recruitRequestDto1 = new RecruitRequestDto("프론트엔드", 1);
        RecruitRequestDto recruitRequestDto2 = new RecruitRequestDto("백엔드", 2);
        RecruitRequestDto recruitRequestDto3 = new RecruitRequestDto("디자이너", 3);
        List<RecruitRequestDto> recruitRequestDtoList1 = Arrays.asList(recruitRequestDto1, recruitRequestDto2, recruitRequestDto3);
        List<RecruitRequestDto> recruitRequestDtoList2 = Arrays.asList(recruitRequestDto1, recruitRequestDto2, recruitRequestDto3);

        ProjectRequestDto projectRequestDto1 = new ProjectRequestDto("test1", 0, "#웰빙", "hi!", "1 month", "#Java#Spring", "plan1", "#hashtag1", image.getName(), image.getImageUrl(), recruitRequestDtoList1);
        ProjectRequestDto projectRequestDto2 = new ProjectRequestDto("test2", 0, "#스포츠", "hi!!", "2 month", "#JavaScript#Vue.js", "plan2", "#hashtag2", image.getName(), image.getImageUrl(), recruitRequestDtoList2);
        Long projectId1 = projectService.makeProject(teamLeader.getId(), image.getId(), projectRequestDto1);
        Long projectId2 = projectService.makeProject(teamLeader.getId(), image.getId(), projectRequestDto2);

        // 회원 1,2,3 -> 프로젝트 1 & 회원 4,5 -> 프로젝트 2
        ProjectUser projectUser1 = ProjectUser.createProjectUser(member1, projectRepository.getReferenceById(projectId1));
        ProjectUser projectUser2 = ProjectUser.createProjectUser(member2, projectRepository.getReferenceById(projectId1));
        ProjectUser projectUser3 = ProjectUser.createProjectUser(member3, projectRepository.getReferenceById(projectId1));
        ProjectUser projectUser4 = ProjectUser.createProjectUser(member4, projectRepository.getReferenceById(projectId2));
        ProjectUser projectUser5 = ProjectUser.createProjectUser(member5, projectRepository.getReferenceById(projectId2));
        projectUserRepository.save(projectUser1);
        projectUserRepository.save(projectUser2);
        projectUserRepository.save(projectUser3);
        projectUserRepository.save(projectUser4);
        projectUserRepository.save(projectUser5);

        projectService.endProject(projectId1); // 프로젝트 1 종료 처리

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
        Image image = new Image("default-image", "url");
        imageRepository.save(image);

        User teamLeader = User.createUser("leader@gmail.com", encoder.encode("pw"), "tester1", "job");
        User user = User.createUser("user@gmail.com", encoder.encode("pw"), "tester2", "job");
        teamLeader.updateImage(image);
        user.updateImage(image);
        userRepository.save(teamLeader);
        userRepository.save(user);

        TokenDto token = signService.login("user@gmail.com", "pw");

        RecruitRequestDto recruitRequestDto1 = new RecruitRequestDto("프론트엔드", 1);
        RecruitRequestDto recruitRequestDto2 = new RecruitRequestDto("백엔드", 2);
        RecruitRequestDto recruitRequestDto3 = new RecruitRequestDto("디자이너", 3);
        List<RecruitRequestDto> recruitRequestDtoList = Arrays.asList(recruitRequestDto1, recruitRequestDto2, recruitRequestDto3);

        ProjectRequestDto projectRequestDto = new ProjectRequestDto("test", 0, "#웰빙", "hi!", "1 month", "#Java#Spring", "plan", "#hashtag", image.getName(), image.getImageUrl(), recruitRequestDtoList);
        Long projectId = projectService.makeProject(teamLeader.getId(), image.getId(), projectRequestDto);

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
}
