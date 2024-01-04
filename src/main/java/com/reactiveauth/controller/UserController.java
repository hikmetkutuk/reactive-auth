package com.reactiveauth.controller;

import com.reactiveauth.dto.request.AuthRequest;
import com.reactiveauth.dto.request.UserRequest;
import com.reactiveauth.dto.response.UserResponse;
import com.reactiveauth.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<?>> register(@Valid @RequestBody UserRequest userRequest) {
        return userService.register(userRequest);
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<?>> register(@Valid @RequestBody AuthRequest authRequest) {
        return userService.login(authRequest);
    }

    @GetMapping("/list")
    public Flux<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }
}
