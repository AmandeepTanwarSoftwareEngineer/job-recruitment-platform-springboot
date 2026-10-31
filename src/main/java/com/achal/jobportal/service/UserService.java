package com.achal.jobportal.service;


import com.achal.jobportal.dto.CreateUserRequest;
import com.achal.jobportal.dto.UserResponse;
import com.achal.jobportal.entity.User;
import com.achal.jobportal.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse createUser(CreateUserRequest request) {
        String userEmail = request.getEmail();

        // 1. Stop if the email is ALREADY used (remove the "!")
        if (userRepository.existsByEmail(userEmail)) {
            throw new RuntimeException("Email already exists");  // replace with your own exception later
        }

        // 2. Fill the user with data from the request (yours was empty)
        User user = new User();
        user.setName(request.getName());
        user.setEmail(userEmail);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        // 3. SAVE here
        User savedUser = userRepository.save(user);

        // 4. Build the response from the SAVED user
        return mapToResponse(savedUser);
    }

    // Copies only the safe fields from User. The password is never sent back.
    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }

}
