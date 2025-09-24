package com.barogo.delivery.api.controller;

import com.barogo.delivery.api.service.MemberService;
import com.barogo.delivery.auth.JwtTokenProvider;
import com.barogo.delivery.domain.Member;
import com.barogo.delivery.response.exception.BusinessException;
import com.barogo.delivery.response.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    /**
     * Test use case: Successful login
     */
    @Test
    void testLogin_Success() throws Exception {
        // Arrange
        String loginId = "testUser";
        String password = "strongpassword123";
        String accessToken = "mockAccessToken";

        Member mockMember = new Member(loginId, password, "Test User");

        Mockito.when(memberService.authenticate(loginId, password)).thenReturn(mockMember);

        Mockito.when(jwtTokenProvider.createAccessToken(
                Mockito.eq(loginId),
                Mockito.any(Map.class)
        )).thenReturn(accessToken);

        String requestBody = """
                {
                    "loginId": "testUser",
                    "password": "strongpassword123"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(jsonPath("$.data.accessToken").value(accessToken));
    }

    /**
     * Test use case: Login fails with invalid credentials
     */
    @Test
    void testLogin_InvalidCredentials() throws Exception {
        // Arrange
        String loginId = "testUser";
        String password = "wrongpassword";

        Mockito.when(memberService.authenticate(loginId, password)).thenThrow(new BusinessException(ErrorCode.UNAUTHORIZED_ERROR, "Invalid credentials"));

        String requestBody = """
                {
                    "loginId": "testUser",
                    "password": "wrongpassword"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test use case: Login fails with invalid input data
     */
    @Test
    void testLogin_InvalidInput() throws Exception {
        // Arrange
        String requestBody = """
                {
                    "loginId": "",
                    "password": "short"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}