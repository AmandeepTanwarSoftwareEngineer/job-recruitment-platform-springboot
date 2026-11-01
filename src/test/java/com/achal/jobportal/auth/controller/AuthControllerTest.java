package com.achal.jobportal.auth.controller;

import com.achal.jobportal.auth.dto.AuthResponse;
import com.achal.jobportal.auth.service.AuthService;
import com.achal.jobportal.dto.UserResponse;
import com.achal.jobportal.entity.Role;
import com.achal.jobportal.exception.DuplicateResourceException;
import com.achal.jobportal.exception.UnauthorizedException;
import com.achal.jobportal.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Web layer + the real SecurityConfig, so this also checks that register and login are reachable without a token.
@WebMvcTest(value = AuthController.class, properties = "app.jwt.secret=test-secret-that-is-at-least-32-bytes")
@Import(SecurityConfig.class)
class AuthControllerTest {

    private static final String REGISTER_URL = "/api/auth/register";
    private static final String LOGIN_URL = "/api/auth/login";

    private static final String VALID_BODY = """
            {"name": "Asha", "email": "asha@example.com", "password": "secret123"}
            """;

    private static final String LOGIN_BODY = """
            {"email": "asha@example.com", "password": "secret123"}
            """;

    private static final UserResponse ASHA = new UserResponse(1L, "Asha", "asha@example.com", Role.JOB_SEEKER,
            LocalDateTime.of(2026, 11, 1, 10, 0));

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void registerReturns201WithoutAuthentication() throws Exception {
        when(authService.register(any())).thenReturn(new AuthResponse("Registration successful", ASHA));

        mockMvc.perform(post(REGISTER_URL).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Registration successful"))
                .andExpect(jsonPath("$.user.id").value(1))
                .andExpect(jsonPath("$.user.email").value("asha@example.com"))
                .andExpect(jsonPath("$.user.role").value("JOB_SEEKER"))
                .andExpect(jsonPath("$.user.password").doesNotExist())
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    void invalidFieldsReturn400WithFieldErrors() throws Exception {
        String body = """
                {"name": "", "email": "not-an-email", "password": "short"}
                """;

        mockMvc.perform(post(REGISTER_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.name").value("Name is required"))
                .andExpect(jsonPath("$.fieldErrors.email").value("Email must be valid"))
                .andExpect(jsonPath("$.fieldErrors.password").value("Password must be 8-72 characters"));

        verify(authService, never()).register(any());
    }

    @Test
    void unknownRoleReturns400() throws Exception {
        String body = """
                {"name": "Asha", "email": "asha@example.com", "password": "secret123", "role": "ADMIN"}
                """;

        mockMvc.perform(post(REGISTER_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request body is missing or malformed"));
    }

    @Test
    void duplicateEmailReturns409() throws Exception {
        when(authService.register(any())).thenThrow(new DuplicateResourceException("Email already exists"));

        mockMvc.perform(post(REGISTER_URL).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    @Test
    void loginReturns200WithTokenWithoutAuthentication() throws Exception {
        when(authService.login(any())).thenReturn(new AuthResponse("Login successful", ASHA, "header.payload.signature"));

        mockMvc.perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(LOGIN_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.token").value("header.payload.signature"))
                .andExpect(jsonPath("$.user.email").value("asha@example.com"))
                .andExpect(jsonPath("$.user.password").doesNotExist());
    }

    @Test
    void loginWithMissingFieldsReturns400() throws Exception {
        mockMvc.perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").value("Email is required"))
                .andExpect(jsonPath("$.fieldErrors.password").value("Password is required"));

        verify(authService, never()).login(any());
    }

    @Test
    void wrongCredentialsReturn401() throws Exception {
        when(authService.login(any())).thenThrow(new UnauthorizedException("Invalid email or password"));

        mockMvc.perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(LOGIN_BODY))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }
}
