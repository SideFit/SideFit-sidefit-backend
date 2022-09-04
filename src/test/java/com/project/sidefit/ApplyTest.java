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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
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

    @BeforeEach
    void beforeEach() {
        User applier = User.createUser("applier@gmail.com", encoder.encode("pw1"), "applier", "applier");
        User leader = User.createUser("leader@gmail.com", encoder.encode("pw2"), "leader", "leader");
        userRepository.save(applier);
        userRepository.save(leader);

        Image image = new Image("test-image", "image-url");
        imageRepository.save(image);

        Project project = createProject(leader, image, "test", 0, "#스포츠", "This is test project", "1 month", "#Java#Spring", "plan", "#hashtag", true);
        projectRepository.save(project);
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/project/apply")
    void apply_test() throws Exception {
        //given
        User applier = userRepository.getReferenceById(1L);
        User leader = userRepository.getReferenceById(2L);
        TokenDto token = signService.login("applier@gmail.com", "pw1");

        Project project = projectRepository.getReferenceById(1L);
        ApplyRequestDto applyRequestDto = new ApplyRequestDto("백엔드", "열심히 하겠습니다!");

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
                                fieldWithPath("jobGroup").type(STRING).description("직군"),
                                fieldWithPath("comment").type(STRING).description("한마디")
                        ),
                        responseFields(
                                fieldWithPath("success").type(BOOLEAN).description("성공 여부"),
                                fieldWithPath("code").type(NUMBER).description("상태 코드"),
                                fieldWithPath("result.data").type(NUMBER).description("지원 id")
                        )
                ));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/project/invite")
    void invite_test() throws Exception {
        //given
        User user = userRepository.getReferenceById(1L);
        User leader = userRepository.getReferenceById(2L);
        TokenDto token = signService.login("leader@gmail.com", "pw2");

        Project project = projectRepository.getReferenceById(1L);
        InviteRequestDto inviteRequestDto = new InviteRequestDto("프론트엔드");

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
                                fieldWithPath("jobGroup").type(STRING).description("직군")
                        ),
                        responseFields(
                                fieldWithPath("success").type(BOOLEAN).description("성공 여부"),
                                fieldWithPath("code").type(NUMBER).description("상태 코드"),
                                fieldWithPath("result.data").type(NUMBER).description("초대 id")
                        )
                ));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/project/apply-response")
    void apply_response_test() throws Exception {
        //given
        User applier = userRepository.getReferenceById(1L);
        User leader = userRepository.getReferenceById(2L);
        TokenDto token = signService.login("leader@gmail.com", "pw2");

        Project project = projectRepository.getReferenceById(1L);
        ApplyRequestDto applyRequestDto = new ApplyRequestDto("백엔드", "열심히 하겠습니다");
        Long applyId = applyService.applyToTeam(applier.getId(), project.getId(), applyRequestDto);

        //when
        ResultActions result = mockMvc.perform(post("/api/project/apply-response")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-AUTH-TOKEN", token.getAccessToken())
                .param("applyId", String.valueOf(applyId))
                .param("flag", "TRUE")
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("apply_response",
                        requestParameters(
                                parameterWithName("applyId").description("지원 id"),
                                parameterWithName("flag").description("수락, 거절 여부")
                        ),
                        responseFields(
                                fieldWithPath("success").type(BOOLEAN).description("성공 여부"),
                                fieldWithPath("code").type(NUMBER).description("결과 코드"),
                                fieldWithPath("result").type(NULL).description("결과 메세지")
                        )
                ));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/project/invite-response")
    void invite_response_test() throws Exception {
        //given
        User applier = userRepository.getReferenceById(1L);
        User leader = userRepository.getReferenceById(2L);
        TokenDto token = signService.login("applier@gmail.com", "pw1");

        Project project = projectRepository.getReferenceById(1L);
        InviteRequestDto inviteRequestDto = new InviteRequestDto("프론트엔드");
        Long applyId = applyService.inviteToUser(applier.getId(), project.getId(), inviteRequestDto);

        //when
        ResultActions result = mockMvc.perform(post("/api/project/invite-response")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-AUTH-TOKEN", token.getAccessToken())
                .param("applyId", String.valueOf(applyId))
                .param("flag", "TRUE")
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("invite_response",
                        requestParameters(
                                parameterWithName("applyId").description("초대 id"),
                                parameterWithName("flag").description("수락, 거절 여부")
                        ),
                        responseFields(
                                fieldWithPath("success").type(BOOLEAN).description("성공 여부"),
                                fieldWithPath("code").type(NUMBER).description("결과 코드"),
                                fieldWithPath("result").type(NULL).description("결과 메세지")
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