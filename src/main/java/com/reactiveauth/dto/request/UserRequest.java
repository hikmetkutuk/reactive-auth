package com.reactiveauth.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record UserRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String email,
        List<String> roles
) {
}
