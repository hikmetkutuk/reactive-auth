package com.reactiveauth.service;

import com.reactiveauth.dto.request.UserRequest;
import com.reactiveauth.model.User;
import com.reactiveauth.repository.UserRepository;
import com.reactiveauth.security.JwtTokenProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
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
                .map(savedUser -> {
                    // Login automatically after adding user
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            savedUser.getUsername(), savedUser.getPassword(),
                            AuthorityUtils.createAuthorityList(savedUser.getRoles().toArray(new String[0])));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // Create JWT and send to user
                    String jwt = jwtTokenProvider.createToken(authentication);
                    HttpHeaders httpHeaders = new HttpHeaders();
                    httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);
                    Map<String, Object> tokenBody = Map.of("access_token", jwt);
                    return ResponseEntity.ok().headers(httpHeaders).body(tokenBody);
                });
    }
}
