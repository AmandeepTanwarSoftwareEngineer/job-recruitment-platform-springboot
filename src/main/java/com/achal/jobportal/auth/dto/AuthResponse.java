package com.achal.jobportal.auth.dto;

import com.achal.jobportal.dto.UserResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// What the auth endpoints send back. Example for login:
// {
//   "message": "Login successful",
//   "user": { "id": 1, "name": "Asha", "email": "asha@example.com", "role": "JOB_SEEKER", "createdAt": "..." },
//   "token": "eyJhbGciOiJIUzI1NiJ9..."
// }
// Register sends the same shape without "token".
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String message;
    private UserResponse user;

    // Send as "Authorization: Bearer <token>" on requests to protected endpoints.
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String token;

    public AuthResponse(String message, UserResponse user) {
        this(message, user, null);
    }
}
