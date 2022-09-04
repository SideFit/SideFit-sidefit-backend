package com.project.sidefit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.sidefit.config.security.JwtProvider;
import com.project.sidefit.domain.entity.Image;
import com.project.sidefit.domain.entity.User;
import com.project.sidefit.domain.enums.NotificationType;
import com.project.sidefit.domain.repository.ImageRepository;
import com.project.sidefit.domain.repository.UserJpaRepo;
import com.project.sidefit.domain.service.dto.TokenDto;
import com.project.sidefit.domain.service.notification.NotificationService;
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

import static com.project.sidefit.api.dto.NotificationDto.*;
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
public class NotificationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SignService signService;

    @Autowired
    private UserJpaRepo userRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private PasswordEncoder encoder;

    @BeforeEach
    void beforeEach() {
        User sender = User.createUser("sender@gmail.com", encoder.encode("pw1"), "sender", "sender");
        User receiver = User.createUser("receiver@gmail.com", encoder.encode("pw2"), "receiver", "receiver");
        userRepository.save(sender);
        userRepository.save(receiver);

        Image image = new Image("test-image", "image-url");
        sender.updateImage(image);
        imageRepository.save(image);
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/sse/connect")
    void connect_test() throws Exception {
        //given
        TokenDto token = signService.login("receiver@gmail.com", "pw2");

        //when
        ResultActions result = mockMvc.perform(get("/api/sse/connect")
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .header("X-AUTH-TOKEN", token.getAccessToken())
                .header("Last-Event-ID", "")
        );

        //then
//        result.andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/notification/send")
    void sendNotification_test() throws Exception {
        //given
        User receiver = userRepository.getReferenceById(2L);
        NotificationRequestDto notificationRequestDto = new NotificationRequestDto("test", NotificationType.CHAT);
        TokenDto token = signService.login("sender@gmail.com", "pw1");

        //when
        ResultActions result = mockMvc.perform(post("/api/notification/send")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-AUTH-TOKEN", token.getAccessToken())
                .param("receiverId", String.valueOf(receiver.getId()))
                .content(objectMapper.writeValueAsString(notificationRequestDto))
                .accept(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("send_notification",
                        requestParameters(
                                parameterWithName("receiverId").description("수신자 id")
                        ),
                        requestFields(
                                fieldWithPath("content").type(STRING).description("알림 내용"),
                                fieldWithPath("type").type(STRING).description("알림 타입")
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
    @DisplayName("GET /api/notification/list")
    void getNotifications_test() throws Exception {
        //given
        User sender = userRepository.getReferenceById(1L);
        User receiver = userRepository.getReferenceById(2L);
        TokenDto token = signService.login("receiver@gmail.com", "pw2");

        for (int i = 1; i <= 5; i++) {
            NotificationRequestDto notificationDto = new NotificationRequestDto("test" + i, NotificationType.CHAT);
            notificationService.sendNotification(notificationDto, sender.getId(), receiver.getId());
            Thread.sleep(10);
        }

        //when
        ResultActions result = mockMvc.perform(get("/api/notification/list")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-AUTH-TOKEN", token.getAccessToken())
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("get_notifications",
                        responseFields(
                                fieldWithPath("success").type(BOOLEAN).description("성공 여부"),
                                fieldWithPath("code").type(NUMBER).description("결과 코드"),
                                fieldWithPath("result.data[].id").type(NUMBER).description("알림 id"),
                                fieldWithPath("result.data[].senderId").type(NUMBER).description("송신자 id"),
                                fieldWithPath("result.data[].receiverId").type(NUMBER).description("수신자 id"),
                                fieldWithPath("result.data[].content").type(STRING).description("알림 내용"),
                                fieldWithPath("result.data[].type").type(STRING).description("알림 타입"),
                                fieldWithPath("result.data[].createdDate").type(STRING).description("생성 일자"),
                                fieldWithPath("result.data[].lastModifiedDate").type(STRING).description("수정 일자"),
                                fieldWithPath("result.data[].imageId").type(NUMBER).description("송신자 이미지 id"),
                                fieldWithPath("result.data[].imageUrl").type(STRING).description("송신자 이미지 url"),
                                fieldWithPath("result.data[].nickname").type(STRING).description("송신자 닉네임")
                        )
                ));
    }
}