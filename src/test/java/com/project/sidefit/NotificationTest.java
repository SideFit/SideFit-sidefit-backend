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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static com.project.sidefit.api.dto.NotificationDto.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
@SpringBootTest
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
    private ImageRepository imageRepository;

    @Autowired
    private UserJpaRepo userRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private PasswordEncoder encoder;

    @Test
    @WithMockUser
    @DisplayName("GET /api/sse/connect")
    void connect_test() throws Exception {
        //given
        User user = User.createUser("user", encoder.encode("pw"), "sender", "job");
        userRepository.save(user);
        TokenDto token = signService.login("user", "pw");

        //when
        ResultActions result = mockMvc.perform(get("/api/sse/connect")
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .header("X-AUTH-TOKEN", token.getAccessToken())
                .header("Last-Event-ID", "")
                .param("userId", String.valueOf(user.getId()))
        );

        //then
//        result.andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/notification/send")
    void sendNotification_test() throws Exception {
        //given
        Image image = new Image("image", "url");
        User sender = User.createUser("sender", encoder.encode("pw"), "sender", "job");
        User receiver = User.createUser("receiver", encoder.encode("pw"), "receiver", "job");
        imageRepository.save(image);
        userRepository.save(sender);
        userRepository.save(receiver);

        TokenDto token = signService.login("sender", "pw");

        String json = "{\n" +
                " \"content\" : \"test\",\n" +
                " \"type\" : \"CHAT\"\n" +
                "}\n";

        //when
        ResultActions result = mockMvc.perform(post("/api/notification/send")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-AUTH-TOKEN", token.getAccessToken())
                .param("receiverId", "3")
                .content(json)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("send_notification",
                        requestParameters(
                                parameterWithName("receiverId").description("수신자 id")
                        ),
                        requestFields(
                                fieldWithPath("content").type(JsonFieldType.STRING).description("알림 내용"),
                                fieldWithPath("type").type(JsonFieldType.STRING).description("알림 타입")
                        ),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("결과 코드"),
                                fieldWithPath("result.data.[].id").type(JsonFieldType.NUMBER).description("알림 id"),
                                fieldWithPath("result.data.[].senderId").type(JsonFieldType.NUMBER).description("송신자 id"),
                                fieldWithPath("result.data.[].receiverId").type(JsonFieldType.NUMBER).description("수신자 id"),
                                fieldWithPath("result.data.[].content").type(JsonFieldType.STRING).description("알림 내용"),
                                fieldWithPath("result.data.[].type").type(JsonFieldType.STRING).description("알림 타입"),
                                fieldWithPath("result.data.[].createdDate").type(JsonFieldType.STRING).description("생성 일자"),
                                fieldWithPath("result.data.[].lastModifiedDate").type(JsonFieldType.STRING).description("수정 일자")
                        )
                ));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/notification/list")
    void getNotifications_test() throws Exception {
        //given
        Image image = new Image("image", "url");
        User sender = User.createUser("sender", encoder.encode("pw"), "sender", "job");
        User receiver = User.createUser("receiver", encoder.encode("pw"), "receiver", "job");
        sender.updateImage(image);
        imageRepository.save(image);
        userRepository.save(sender);
        userRepository.save(receiver);

        TokenDto token = signService.login("receiver", "pw");

        for (int i = 1; i <= 5; i++) {
            NotificationRequestDto dto = new NotificationRequestDto("test" + i, NotificationType.CHAT);
            notificationService.sendNotification(dto, sender.getId(), receiver.getId());
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
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("결과 코드"),
                                fieldWithPath("result.data.[].id").type(JsonFieldType.NUMBER).description("알림 id"),
                                fieldWithPath("result.data.[].senderId").type(JsonFieldType.NUMBER).description("송신자 id"),
                                fieldWithPath("result.data.[].receiverId").type(JsonFieldType.NUMBER).description("수신자 id"),
                                fieldWithPath("result.data.[].content").type(JsonFieldType.STRING).description("알림 내용"),
                                fieldWithPath("result.data.[].type").type(JsonFieldType.STRING).description("알림 타입"),
                                fieldWithPath("result.data.[].createdDate").type(JsonFieldType.STRING).description("생성 일자"),
                                fieldWithPath("result.data.[].lastModifiedDate").type(JsonFieldType.STRING).description("수정 일자"),
                                fieldWithPath("result.data.[].imageId").type(JsonFieldType.NUMBER).description("송신자 이미지 id"),
                                fieldWithPath("result.data.[].nickname").type(JsonFieldType.STRING).description("송신자 닉네임")
                        )
                ));
    }
}
