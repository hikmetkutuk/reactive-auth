package com.reactiveauth.service;

import com.reactiveauth.dto.request.AuthRequest;
import com.reactiveauth.dto.request.UserRequest;
import com.reactiveauth.dto.response.UserResponse;
import com.reactiveauth.model.User;
import com.reactiveauth.repository.UserRepository;
import com.reactiveauth.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final ReactiveAuthenticationManager authenticationManager;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository, JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder, ReactiveAuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public Mono<ResponseEntity<?>> register(UserRequest userRequest) {
        User newUser = User.builder()
                .id(UUID.randomUUID())
                .username(userRequest.username())
                .password(passwordEncoder.encode(userRequest.password()))
                .email(userRequest.email())
                .roles(userRequest.roles())
                .build();

        return userRepository.save(newUser)
                .flatMap(savedUser -> {
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            savedUser.getUsername(), savedUser.getPassword(),
                            AuthorityUtils.createAuthorityList(savedUser.getRoles().toArray(new String[0])));

                    return createJwtResponse(authentication);
                })
                .onErrorResume(Exception.class, e ->
                        Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(Map.of("error", "An error occurred during registration"))));
    }

    public Mono<ResponseEntity<?>> login(AuthRequest authRequest) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                authRequest.username(), authRequest.password());

        return authenticationManager.authenticate(authentication)
                .flatMap(this::createJwtResponse)
                .onErrorResume(AuthenticationException.class, e ->
                        Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(Map.of("error", "Invalid credentials"))));
    }

    public Flux<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .flatMap(user -> Mono.just(new UserResponse(user.getUsername(), user.getEmail(), user.getRoles())))
                .doOnTerminate(() -> log.info("User fetching process completed"))
                .onErrorResume(DataAccessException.class, e -> {
                    log.error("An error occurred while fetching users from the database", e);
                    return Flux.empty();
                })
                .onErrorResume(Exception.class, e -> {
                    log.error("An unexpected error occurred while fetching users", e);
                    return Flux.empty();
                });
    }

    private Mono<ResponseEntity<?>> createJwtResponse(Authentication authentication) {
        String jwt = jwtTokenProvider.createToken(authentication);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);
        Map<String, Object> tokenBody = Map.of("access_token", jwt);
        return Mono.just(ResponseEntity.ok().headers(httpHeaders).body(tokenBody));
    }
}
