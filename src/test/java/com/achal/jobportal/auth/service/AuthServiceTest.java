package com.achal.jobportal.auth.service;

import com.achal.jobportal.auth.dto.AuthResponse;
import com.achal.jobportal.auth.dto.LoginRequest;
import com.achal.jobportal.auth.dto.RegisterRequest;
import com.achal.jobportal.entity.Role;
import com.achal.jobportal.entity.User;
import com.achal.jobportal.exception.BadRequestException;
import com.achal.jobportal.exception.DuplicateResourceException;
import com.achal.jobportal.exception.UnauthorizedException;
import com.achal.jobportal.repository.UserRepository;
import com.achal.jobportal.security.JwtProperties;
import com.achal.jobportal.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Plain unit test: the repository is mocked; BCrypt and the JWT signing/checking are the real ones.
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final SecretKey JWT_KEY =
            new SecretKeySpec("test-secret-that-is-at-least-32-bytes".getBytes(StandardCharsets.UTF_8), "HmacSHA256");

    @Mock
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final JwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(JWT_KEY).build();

    private AuthService authService;

    @BeforeEach
    void setUp() {
        JwtService jwtService = new JwtService(NimbusJwtEncoder.withSecretKey(JWT_KEY).build(),
                new JwtProperties("unused-here", Duration.ofHours(1)));
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void registerSavesUserWithHashedPasswordAndNormalizedEmail() {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        AuthResponse response = authService.register(
                new RegisterRequest("Asha", " Asha@Example.com ", "secret123", null));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertThat(saved.getEmail()).isEqualTo("asha@example.com");
        assertThat(saved.getPassword()).isNotEqualTo("secret123");
        assertThat(passwordEncoder.matches("secret123", saved.getPassword())).isTrue();
        assertThat(saved.getRole()).isEqualTo(Role.JOB_SEEKER);

        assertThat(response.getMessage()).isEqualTo("Registration successful");
        assertThat(response.getUser().getId()).isEqualTo(1L);
        assertThat(response.getUser().getEmail()).isEqualTo("asha@example.com");
        assertThat(response.getUser().getRole()).isEqualTo(Role.JOB_SEEKER);
    }

    @Test
    void registerKeepsRequestedRole() {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = authService.register(
                new RegisterRequest("Ravi", "ravi@example.com", "secret123", Role.RECRUITER));

        assertThat(response.getUser().getRole()).isEqualTo(Role.RECRUITER);
    }

    @Test
    void registerRejectsAlreadyUsedEmail() {
        when(userRepository.existsByEmail("asha@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(
                new RegisterRequest("Asha", "ASHA@example.com", "secret123", null)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Email already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void registerRejectsPasswordLongerThan72Bytes() {
        // 40 characters passes @Size, but each "é" is 2 bytes in UTF-8, so this is 80 bytes.
        String password = "é".repeat(40);

        assertThatThrownBy(() -> authService.register(
                new RegisterRequest("Asha", "asha@example.com", password, null)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Password is too long");

        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void loginReturnsSignedTokenForCorrectPassword() {
        User user = existingUser(7L, "asha@example.com", "secret123", Role.RECRUITER);
        when(userRepository.findByEmail("asha@example.com")).thenReturn(Optional.of(user));

        // Email is matched the same way register stores it: trimmed and lower-cased.
        AuthResponse response = authService.login(new LoginRequest(" Asha@Example.com ", "secret123"));

        assertThat(response.getMessage()).isEqualTo("Login successful");
        assertThat(response.getUser().getId()).isEqualTo(7L);

        Jwt jwt = jwtDecoder.decode(response.getToken()); // throws if the signature is wrong
        assertThat(jwt.getSubject()).isEqualTo("7");
        assertThat(jwt.getClaimAsString("email")).isEqualTo("asha@example.com");
        assertThat(jwt.getClaimAsString("role")).isEqualTo("RECRUITER");
        assertThat(jwt.getExpiresAt()).isAfter(Instant.now().plus(Duration.ofMinutes(59)));
    }

    @Test
    void loginRejectsWrongPassword() {
        User user = existingUser(7L, "asha@example.com", "secret123", Role.JOB_SEEKER);
        when(userRepository.findByEmail("asha@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("asha@example.com", "wrong-password")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void loginRejectsUnknownEmailWithSameMessageAsWrongPassword() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("nobody@example.com", "secret123")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void loginRejectsPasswordLongerThan72BytesWithoutCallingBcrypt() {
        assertThatThrownBy(() -> authService.login(new LoginRequest("asha@example.com", "é".repeat(40))))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid email or password");

        verify(userRepository, never()).findByEmail(anyString());
    }

    private User existingUser(Long id, String email, String rawPassword, Role role) {
        User user = new User();
        user.setId(id);
        user.setName("Asha");
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        return user;
    }
}
