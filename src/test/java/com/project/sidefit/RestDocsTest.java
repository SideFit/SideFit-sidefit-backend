package com.project.sidefit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@AutoConfigureRestDocs
public class RestDocsTest {

    @Autowired
    private MockMvc mockMvc;

//    @MockBean

    @Test
    @DisplayName("RestDocs 테스트")
    public void healthcheck_테스트() throws Exception {

        ResultActions result = this.mockMvc.perform(get("/sidefit/healthcheck")
                .accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andDo(document("healthcheck", responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 유무"),
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("상태 코드"),
                                fieldWithPath("result.data").type(JsonFieldType.STRING).description("healthcheck msg")
                        ))
                );
    }
}
