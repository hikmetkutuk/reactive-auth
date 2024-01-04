package com.reactiveauth.dto.response;

import java.util.List;

public record UserResponse(
        String username,
        String email,
        List<String> roles
) {
}
