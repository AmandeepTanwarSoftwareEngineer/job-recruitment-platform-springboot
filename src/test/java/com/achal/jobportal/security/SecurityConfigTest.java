package com.achal.jobportal.security;

import com.achal.jobportal.controller.UserController;
import com.achal.jobportal.dto.UserResponse;
import com.achal.jobportal.entity.Role;
import com.achal.jobportal.entity.User;
import com.achal.jobportal.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Checks that a token from login actually opens a protected endpoint (GET /api/users/{id}),
// and that requests without a valid one are turned away.
@WebMvcTest(value = UserController.class, properties = "app.jwt.secret=" + SecurityConfigTest.SECRET)
@Import({SecurityConfig.class, JwtService.class})
class SecurityConfigTest {

    static final String SECRET = "test-secret-that-is-at-least-32-bytes";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JwtEncoder jwtEncoder;

    @MockitoBean
    private UserService userService;

    @Test
    void protectedEndpointWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointWithLoginTokenIsAllowed() throws Exception {
        when(userService.findUser(1L)).thenReturn(new UserResponse(1L, "Asha", "asha@example.com",
                Role.JOB_SEEKER, LocalDateTime.of(2026, 11, 1, 10, 0)));

        mockMvc.perform(get("/api/users/1").header("Authorization", "Bearer " + jwtService.generateToken(asha())))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.email").value("asha@example.com"));
    }

    @Test
    void tokenSignedWithAnotherKeyReturns401() throws Exception {
        var otherKey = new SecretKeySpec("some-other-secret-at-least-32-bytes".getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        var forger = new JwtService(NimbusJwtEncoder.withSecretKey(otherKey).build(),
                new JwtProperties("unused-here", Duration.ofHours(1)));

        mockMvc.perform(get("/api/users/1").header("Authorization", "Bearer " + forger.generateToken(asha())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void expiredTokenReturns401() throws Exception {
        // Correctly signed, but expired 5 minutes ago (more than the 60 seconds of clock skew the check allows).
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject("1")
                .issuedAt(now.minus(Duration.ofHours(1)))
                .expiresAt(now.minus(Duration.ofMinutes(5)))
                .build();
        String expired = jwtEncoder.encode(
                JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();

        mockMvc.perform(get("/api/users/1").header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized());
    }

    private static User asha() {
        User user = new User();
        user.setId(1L);
        user.setEmail("asha@example.com");
        user.setRole(Role.JOB_SEEKER);
        return user;
    }
}
