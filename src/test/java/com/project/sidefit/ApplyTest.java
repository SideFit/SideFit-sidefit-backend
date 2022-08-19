package com.project.sidefit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.sidefit.domain.entity.Image;
import com.project.sidefit.domain.entity.Project;
import com.project.sidefit.domain.entity.User;
import com.project.sidefit.domain.repository.ImageRepository;
import com.project.sidefit.domain.repository.UserJpaRepo;
import com.project.sidefit.domain.repository.project.ProjectRepository;
import com.project.sidefit.domain.service.ApplyService;
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
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static com.project.sidefit.api.dto.ApplyDto.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
@SpringBootTest
public class ApplyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplyService applyService;

    @Autowired
    private SignService signService;

    @Autowired
    private UserJpaRepo userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Test
    @WithMockUser
    @DisplayName("POST /api/project/apply")
    void apply_test() throws Exception {
        //given
        User applier = User.createUser("applier", encoder.encode("pw"), "applier", "job");
        User teamLeader = User.createUser("teamLeader", encoder.encode("pw"), "teamLeader", "job");
        userRepository.save(applier);
        userRepository.save(teamLeader);

        Image image = new Image("image", "url");
        imageRepository.save(image);

        Project project = createProject(teamLeader, image);
        projectRepository.save(project);

        ApplyRequestDto applyRequestDto = new ApplyRequestDto("백엔드", "열심히 하겠습니다!");

        TokenDto token = signService.login("applier", "pw");

        //when
        ResultActions result = mockMvc.perform(post("/api/project/apply")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-AUTH-TOKEN", token.getAccessToken())
                .param("projectId", String.valueOf(project.getId()))
                .content(objectMapper.writeValueAsString(applyRequestDto))
                .accept(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("apply_to_project",
                        requestParameters(
                                parameterWithName("projectId").description("프로젝트 id")
                        ),
                        requestFields(
                                fieldWithPath("jobGroup").type(JsonFieldType.STRING).description("직군"),
                                fieldWithPath("comment").type(JsonFieldType.STRING).description("한마디")
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
    @DisplayName("POST /api/project/invite")
    void invite_test() throws Exception {
        //given
        User user = User.createUser("user", encoder.encode("pw"), "user", "job");
        User teamLeader = User.createUser("teamLeader", encoder.encode("pw"), "teamLeader", "job");
        userRepository.save(user);
        userRepository.save(teamLeader);

        Image image = new Image("image", "url");
        imageRepository.save(image);

        Project project = createProject(teamLeader, image);
        projectRepository.save(project);

        InviteRequestDto inviteRequestDto = new InviteRequestDto("프론트엔드");

        TokenDto token = signService.login("teamLeader", "pw");

        //when
        ResultActions result = mockMvc.perform(post("/api/project/invite")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-AUTH-TOKEN", token.getAccessToken())
                .param("receiverId", String.valueOf(user.getId()))
                .param("projectId", String.valueOf(project.getId()))
                .content(objectMapper.writeValueAsString(inviteRequestDto))
                .accept(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("invite_user",
                        requestParameters(
                                parameterWithName("receiverId").description("수신자 id"),
                                parameterWithName("projectId").description("프로젝트 id")
                        ),
                        requestFields(
                                fieldWithPath("jobGroup").type(JsonFieldType.STRING).description("직군")
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
    @DisplayName("GET /api/project/apply-response")
    void apply_response_test() throws Exception {
        //given
        User applier = User.createUser("applier", encoder.encode("pw"), "applier", "job");
        User teamLeader = User.createUser("teamLeader", encoder.encode("pw"), "teamLeader", "job");
        userRepository.save(applier);
        userRepository.save(teamLeader);

        Image image = new Image("image", "url");
        imageRepository.save(image);

        Project project = createProject(teamLeader, image);
        projectRepository.save(project);

        ApplyRequestDto applyRequestDto = new ApplyRequestDto("백엔드", "열심히 하겠습니다");
        Long applyId = applyService.applyToTeam(applier.getId(), project.getId(), applyRequestDto);

        TokenDto token = signService.login("teamLeader", "pw");

        //when
        ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.get("/api/project/apply-response/{applyId}", String.valueOf(applyId))
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-AUTH-TOKEN", token.getAccessToken())
                .param("flag", "TRUE")
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("apply_response",
                        pathParameters(
                                parameterWithName("applyId").description("지원 id")
                        ),
                        requestParameters(
                                parameterWithName("flag").description("수락, 거절 여부")
                        ),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("결과 코드"),
                                fieldWithPath("result.data.id").type(JsonFieldType.NUMBER).description("지원 id"),
                                fieldWithPath("result.data.userId").type(JsonFieldType.NUMBER).description("유저 id"),
                                fieldWithPath("result.data.projectId").type(JsonFieldType.NUMBER).description("프로젝트 id"),
                                fieldWithPath("result.data.status").type(JsonFieldType.NUMBER).description("지원 상태"),
                                fieldWithPath("result.data.createdDate").type(JsonFieldType.STRING).description("생성 일자"),
                                fieldWithPath("result.data.lastModifiedDate").type(JsonFieldType.STRING).description("수정 일자")
                        )
                ));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/project/invite-response")
    void invite_response_test() throws Exception {
        //given
        User user = User.createUser("user", encoder.encode("pw"), "user", "job");
        User teamLeader = User.createUser("teamLeader", encoder.encode("pw"), "teamLeader", "job");
        userRepository.save(user);
        userRepository.save(teamLeader);

        Image image = new Image("image", "url");
        imageRepository.save(image);

        Project project = createProject(teamLeader, image);
        projectRepository.save(project);

        InviteRequestDto inviteRequestDto = new InviteRequestDto("프론트엔드");
        Long applyId = applyService.inviteToUser(user.getId(), project.getId(), inviteRequestDto);

        TokenDto token = signService.login("user", "pw");

        //when
        ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.get("/api/project/invite-response/{applyId}", String.valueOf(applyId))
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-AUTH-TOKEN", token.getAccessToken())
                .param("flag", "TRUE")
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("invite_response",
                        pathParameters(
                                parameterWithName("applyId").description("지원 id")
                        ),
                        requestParameters(
                                parameterWithName("flag").description("수락, 거절 여부")
                        ),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("결과 코드"),
                                fieldWithPath("result.data.id").type(JsonFieldType.NUMBER).description("초대 id"),
                                fieldWithPath("result.data.userId").type(JsonFieldType.NUMBER).description("유저 id"),
                                fieldWithPath("result.data.projectId").type(JsonFieldType.NUMBER).description("프로젝트 id"),
                                fieldWithPath("result.data.status").type(JsonFieldType.NUMBER).description("초대 상태"),
                                fieldWithPath("result.data.createdDate").type(JsonFieldType.STRING).description("생성 일자"),
                                fieldWithPath("result.data.lastModifiedDate").type(JsonFieldType.STRING).description("수정 일자")
                        )
                ));
    }

    private Project createProject(User teamLeader, Image image) {
        return Project.builder()
                .user(teamLeader)
                .image(image)
                .title("title")
                .type(0)
                .field("field")
                .introduction("hi")
                .period("1 month")
                .stack("#Java")
                .meetingPlan("plan")
                .hashtag("#test")
                .build();
    }
}