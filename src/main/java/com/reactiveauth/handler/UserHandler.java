package com.reactiveauth.handler;

import com.reactiveauth.dto.request.AuthRequest;
import com.reactiveauth.dto.request.UserRequest;
import com.reactiveauth.dto.response.UserResponse;
import com.reactiveauth.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class UserHandler {
    private final UserService userService;

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    public Mono<ServerResponse> handleRegister(ServerRequest serverRequest) {
        return serverRequest
                .bodyToMono(UserRequest.class)
                .flatMap(userService::register)
                .flatMap(registeredUser -> ServerResponse.ok().bodyValue(registeredUser))
                .onErrorResume(error -> ServerResponse.badRequest().bodyValue(error.getMessage()));
    }

    public Mono<ServerResponse> handleLogin(ServerRequest serverRequest) {
        return serverRequest
                .bodyToMono(AuthRequest.class)
                .flatMap(userService::login)
                .flatMap(registeredUser -> ServerResponse.ok().bodyValue(registeredUser))
                .onErrorResume(error -> ServerResponse.badRequest().bodyValue(error.getMessage()));
    }

    public Mono<ServerResponse> handleGetAllUsers(ServerRequest request) {
        Flux<UserResponse> users = userService.getAllUsers();
        return ServerResponse.ok().body(users, UserResponse.class);
    }
}
