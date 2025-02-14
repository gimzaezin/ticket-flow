package com.gimzazin.tiketflow.users.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gimzazin.tiketflow.users.dto.UserCreateRequestDto;
import com.gimzazin.tiketflow.users.dto.UserResponseDto;
import com.gimzazin.tiketflow.users.service.UserService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureRestDocs
@ExtendWith({RestDocumentationExtension.class, MockitoExtension.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void createUserTest() throws Exception {
        UserResponseDto responseDto =
                UserResponseDto
                        .builder()
                        .userId(1L)
                        .name("홍길동")
                        .email("example@gmail.com")
                        .phone("010-1234-5678")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

        when(userService.createUser(any(UserCreateRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"홍길동\", \"email\": \"example@gmail.com\", \"phone\": \"010-1234-5678\"}"))
                .andExpect(status().isCreated())
                .andDo(document("create-user",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("name").description("사용자 이름"),
                                fieldWithPath("email").description("사용자 이메일"),
                                fieldWithPath("phone").description("사용자 전화번호")
                        ),
                        responseFields(
                                fieldWithPath("userId").description("생성된 사용자 ID"),
                                fieldWithPath("name").description("사용자 이름"),
                                fieldWithPath("email").description("사용자 이메일"),
                                fieldWithPath("phone").description("사용자 전화번호"),
                                fieldWithPath("createdAt").description("생성 시간"),
                                fieldWithPath("updatedAt").description("수정 시간")
                        )
                ));

    }

    @Test
    void getAllUsersTest() throws Exception {
        List<UserResponseDto> users = List.of(
                new UserResponseDto(1L, "홍길동", "example@gmail.com", "010-1234-5678", LocalDateTime.now(), LocalDateTime.now()),
                new UserResponseDto(2L, "김철수", "example@naver.com", "010-5678-1234", LocalDateTime.now(), LocalDateTime.now())
        );

        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/v1/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("get-all-users",
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("[].userId").description("사용자 ID"),
                                fieldWithPath("[].name").description("사용자 이름"),
                                fieldWithPath("[].email").description("사용자 이메일"),
                                fieldWithPath("[].phone").description("사용자 전화번호"),
                                fieldWithPath("[].createdAt").description("생성 시간"),
                                fieldWithPath("[].updatedAt").description("수정 시간")
                        )
                ));
    }

    @Test
    void getUserByIdTest() throws Exception {
        UserResponseDto responseDto =
                new UserResponseDto(1L, "홍길동", "hong@example.com", "010-1234-5678", LocalDateTime.now(), LocalDateTime.now());

        when(userService.getUserById(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/api/v1/users/{userId}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("get-user-by-id",
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("userId").description("사용자 ID"),
                                fieldWithPath("name").description("사용자 이름"),
                                fieldWithPath("email").description("사용자 이메일"),
                                fieldWithPath("phone").description("사용자 전화번호"),
                                fieldWithPath("createdAt").description("생성 시간"),
                                fieldWithPath("updatedAt").description("수정 시간")
                        )
                ));
    }

    @Test
    void deleteUserTest() throws Exception {
        mockMvc.perform(delete("/api/v1/users/{userId}", 1L))
                .andExpect(status().isNoContent())
                .andDo(document("delete-user"));
    }
}