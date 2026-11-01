package com.achal.jobportal.auth.controller;

import com.achal.jobportal.auth.dto.AuthResponse;
import com.achal.jobportal.auth.dto.LoginRequest;
import com.achal.jobportal.auth.dto.RegisterRequest;
import com.achal.jobportal.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Public endpoints (see SecurityConfig): no token is needed to call them.
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // 201 on success, 400 on invalid input, 409 if the email is already registered.
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 200 with a token on success, 400 if a field is missing, 401 if the email or password is wrong.
    // Nothing is created, so this is 200 rather than 201.
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
