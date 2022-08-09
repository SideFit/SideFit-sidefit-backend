package com.project.sidefit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.sidefit.api.controller.NotificationApiController;
import com.project.sidefit.config.security.JwtProvider;
import com.project.sidefit.domain.entity.Image;
import com.project.sidefit.domain.entity.User;
import com.project.sidefit.domain.enums.NotificationType;
import com.project.sidefit.domain.service.dto.TokenDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

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
    private NotificationApiController notificationApiController;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private EntityManager em;
    
    @Test
    @WithMockUser
    @DisplayName("GET /api/sse/connect/{userId}")
    void connect_test() throws Exception {
        //given
        Image image = new Image("testImage", "");
        User user = new User(image, "tester");
        em.persist(image);
        em.persist(user); // id: 2

        TokenDto tokenDto = jwtProvider.createTokenDto(user.getId(), List.of("ROLE_ADMIN"));
        String token = tokenDto.getAccessToken();
        String userId = String.valueOf(user.getId());

        //when
        ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.get("/api/sse/connect/" + userId)
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .header("X-AUTH-TOKEN", token)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("sse connection",
                        pathParameters(
                                parameterWithName("userId").description("사옹자 id")
                        ),
                        responseFields(
                                fieldWithPath("id").description("sse emitter ID"),
                                fieldWithPath("event").description("이벤트 이름"),
                                fieldWithPath("data").description("데이터")
                        )
                ));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/notification/send")
    void sendNotification_test() throws Exception {
        //given
        Image image = new Image("testImage", "");
        User sender = new User(image, "sender");
        User receiver = new User(image, "receiver");
        em.persist(image);
        em.persist(sender); // id: 2
        em.persist(receiver); // id: 3

        String json = "{\n" +
                " \"senderId\" : \"2\",\n" +
                " \"receiverId\" : \"3\",\n" +
                " \"content\" : \"test\",\n" +
                " \"type\" : \"CHAT\"\n" +
                "}\n";

        //when
        ResultActions result = mockMvc.perform(post("/api/notification/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("send notification",
                        requestFields(
                                fieldWithPath("senderId").type(JsonFieldType.STRING).description("송신자 id"),
                                fieldWithPath("receiverId").type(JsonFieldType.STRING).description("수신자 id"),
                                fieldWithPath("content").type(JsonFieldType.STRING).description("알림 내용"),
                                fieldWithPath("type").type(JsonFieldType.STRING).description("알림 타입")
                        ),
                        responseFields(
                                fieldWithPath("id").type(JsonFieldType.NUMBER).description("알림 id"),
                                fieldWithPath("senderId").type(JsonFieldType.NUMBER).description("송신자 id"),
                                fieldWithPath("receiverId").type(JsonFieldType.NUMBER).description("수신자 id"),
                                fieldWithPath("content").type(JsonFieldType.STRING).description("알림 내용"),
                                fieldWithPath("type").type(JsonFieldType.STRING).description("알림 타입"),
                                fieldWithPath("createdDate").type(JsonFieldType.STRING).description("생성 일자"),
                                fieldWithPath("lastModifiedDate").type(JsonFieldType.STRING).description("수정 일자")
                        )
                ));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/notification/list")
    void getNotifications_test() throws Exception {
        //given
        Image image = new Image("testImage", "");
        User sender = new User(image, "sender");
        User receiver = new User(image, "receiver");
        em.persist(image);
        em.persist(sender); // id: 2
        em.persist(receiver); // id: 3

        for (int i = 1; i <= 5; i++) {
            NotificationRequestDto dto = new NotificationRequestDto("2", "3", "test" + i, NotificationType.CHAT);
            notificationApiController.sendNotification(dto);
            Thread.sleep(10);
        }

        //when
        ResultActions result = mockMvc.perform(get("/api/notification/list")
                .contentType(MediaType.APPLICATION_JSON)
                .param("receiverId", "3")
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("get notifications",
                        requestParameters(
                                parameterWithName("receiverId").description("송신자 id")
                        ),
                        responseFields(
                                fieldWithPath("[].id").type(JsonFieldType.NUMBER).description("알림 id"),
                                fieldWithPath("[].senderId").type(JsonFieldType.NUMBER).description("송신자 id"),
                                fieldWithPath("[].receiverId").type(JsonFieldType.NUMBER).description("수신자 id"),
                                fieldWithPath("[].content").type(JsonFieldType.STRING).description("알림 내용"),
                                fieldWithPath("[].type").type(JsonFieldType.STRING).description("알림 타입"),
                                fieldWithPath("[].createdDate").type(JsonFieldType.STRING).description("생성 일자"),
                                fieldWithPath("[].lastModifiedDate").type(JsonFieldType.STRING).description("수정 일자"),
                                fieldWithPath("[].imageId").type(JsonFieldType.NUMBER).description("송신자 이미지 id"),
                                fieldWithPath("[].nickname").type(JsonFieldType.STRING).description("송신자 닉네임")
                        )
                ));
    }
}
