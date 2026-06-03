package com.waypoint.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waypoint.dto.*;
import com.waypoint.exception.BadRequestException;
import com.waypoint.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.waypoint\\.security\\..*"))
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void register_returnsJwtResponse() throws Exception {
        JwtResponse jwtResponse = JwtResponse.builder()
                .token("access-token").refreshToken("rt-uuid")
                .username("newuser").email("new@test.com")
                .role("USER").id(1L).build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(jwtResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"newuser","email":"new@test.com","password":"password123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("rt-uuid"))
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    void register_returns400WhenUsernameMissing() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"new@test.com","password":"password123"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_returns400WhenDuplicateUsername() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new BadRequestException("Username already exists"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"dup","email":"new@test.com","password":"password123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    @Test
    void login_returnsJwtResponse() throws Exception {
        JwtResponse jwtResponse = JwtResponse.builder()
                .token("access-token").refreshToken("rt-uuid")
                .username("testuser").email("test@test.com")
                .role("USER").id(1L).build();

        when(authService.login(any(LoginRequest.class))).thenReturn(jwtResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"testuser","password":"password123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access-token"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void refresh_returnsNewTokens() throws Exception {
        RefreshTokenResponse tokenResponse = RefreshTokenResponse.builder()
                .accessToken("new-access").refreshToken("new-rt").tokenType("Bearer").build();

        when(authService.refresh("valid-rt")).thenReturn(tokenResponse);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"valid-rt"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access"))
                .andExpect(jsonPath("$.refreshToken").value("new-rt"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }
}
