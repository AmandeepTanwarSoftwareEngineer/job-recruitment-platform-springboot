package com.achal.jobportal.exception;

import com.achal.jobportal.controller.UserController;
import com.achal.jobportal.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Web-layer only: the service is mocked, so no database is needed.
@WebMvcTest(UserController.class)
class GlobalExceptionHandlerTest {

    private static final String URL = "/api/users/create";

    private static final String VALID_BODY = """
            {"name": "Asha", "email": "asha@example.com", "password": "secret123"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void invalidFieldsReturn400WithFieldErrors() throws Exception {
        String body = """
                {"name": "", "email": "not-an-email", "password": "short"}
                """;

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value(URL))
                .andExpect(jsonPath("$.fieldErrors.name").value("Name is required"))
                .andExpect(jsonPath("$.fieldErrors.email").value("Email must be valid"))
                .andExpect(jsonPath("$.fieldErrors.password").value("Password must be 8-72 characters"));
    }

    @Test
    void duplicateEmailReturns409() throws Exception {
        when(userService.createUser(any())).thenThrow(new DuplicateResourceException("Email already exists"));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Email already exists"))
                .andExpect(jsonPath("$.fieldErrors").doesNotExist());
    }

    @Test
    void malformedJsonReturns400() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{ not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request body is missing or malformed"));
    }

    @Test
    void unexpectedErrorReturns500WithoutLeakingDetails() throws Exception {
        when(userService.createUser(any())).thenThrow(new IllegalStateException("db password is hunter2"));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Something went wrong. Please try again later."));
    }

    @Test
    void wrongHttpMethodKeeps405() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
    }
}
