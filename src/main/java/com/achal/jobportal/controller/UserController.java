package com.achal.jobportal.controller;


import com.achal.jobportal.dto.CreateUserRequest;
import com.achal.jobportal.dto.UserResponse;
import com.achal.jobportal.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/create")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request){
        UserResponse userResponse = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findUser(@PathVariable Long id){
        UserResponse userResponse = userService.findUser(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(userResponse);
    }


}
