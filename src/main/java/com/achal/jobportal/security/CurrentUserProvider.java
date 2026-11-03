package com.achal.jobportal.security;


import com.achal.jobportal.entity.User;
import com.achal.jobportal.exception.UnauthorizedException;
import com.achal.jobportal.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CurrentUserProvider {
    private final UserRepository userRepository;

    public User getCurrentUser() {
        // getName() is the token's "sub" claim, which JwtService sets to the user's id (not the email).
        Long userId = Long.valueOf(SecurityContextHolder.getContext()
                .getAuthentication()
                .getName());

        // A valid token for a user that was deleted afterwards.
        return userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }

}
