package com.achal.jobportal.auth.service;

import com.achal.jobportal.auth.dto.AuthResponse;
import com.achal.jobportal.auth.dto.LoginRequest;
import com.achal.jobportal.auth.dto.RegisterRequest;
import com.achal.jobportal.dto.UserResponse;
import com.achal.jobportal.entity.Role;
import com.achal.jobportal.entity.User;
import com.achal.jobportal.exception.BadRequestException;
import com.achal.jobportal.exception.DuplicateResourceException;
import com.achal.jobportal.exception.UnauthorizedException;
import com.achal.jobportal.repository.UserRepository;
import com.achal.jobportal.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;               // NEW
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // NEW
import org.springframework.security.core.AuthenticationException;                       // NEW
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
// REMOVED: import java.util.Optional;

@Service
public class AuthService {

    private static final int BCRYPT_MAX_BYTES = 72;

    // Same message for an unknown email and a wrong password, so login can't be used
    // to find out which emails have an account.
    private static final String INVALID_CREDENTIALS = "Invalid email or password";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // still used by register
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager; // NEW

    // REMOVED: dummyPasswordHash (DaoAuthenticationProvider does this itself)

    // CHANGED: AuthenticationManager added
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager; // NEW
    }

    // register: NO CHANGE
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());

        // BCrypt rejects passwords over 72 bytes; turn that into a 400 instead of a 500.
        if (isTooLongForBcrypt(request.getPassword())) {
            throw new BadRequestException("Password is too long");
        }

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already exists"); // -> 409
        }
        // If two requests with the same email get past this check at the same time, the unique
        // constraint on users.email stops the second one and GlobalExceptionHandler returns 409.

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : Role.JOB_SEEKER);

        User saved = userRepository.save(user);
        return new AuthResponse("Registration successful", UserResponse.from(saved));
    }

    // CHANGED: password check now goes through AuthenticationManager
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // Register can't create a password this long, so it can't be right. Rejecting it
        // here also stops BCrypt from throwing on it.
        if (isTooLongForBcrypt(request.getPassword())) {
            throw new UnauthorizedException(INVALID_CREDENTIALS);
        }
        String email = normalizeEmail(request.getEmail());

        // DaoAuthenticationProvider -> CustomUserDetailsService (load user) -> PasswordEncoder (check)
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword()));
        } catch (AuthenticationException e) {
            // Without this catch, GlobalExceptionHandler would turn it into a 500.
            throw new UnauthorizedException(INVALID_CREDENTIALS); // -> 401
        }

        // The token needs the id and role, so load the entity.
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException(INVALID_CREDENTIALS));

        return new AuthResponse("Login successful", UserResponse.from(user), jwtService.generateToken(user));
    }

    // Store and look up emails in one form so "Asha@Example.com" and "asha@example.com" are the same account.
    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean isTooLongForBcrypt(String password) {
        return password.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_BYTES;
    }
}