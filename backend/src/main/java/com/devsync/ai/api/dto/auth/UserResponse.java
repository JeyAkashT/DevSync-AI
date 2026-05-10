package com.devsync.ai.api.dto.auth;

import com.devsync.ai.model.Role;
import com.devsync.ai.model.User;

import java.util.List;
import java.util.UUID;

public record UserResponse(UUID id, String email, String fullName, List<String> roles) {

    public static UserResponse fromEntity(User user) {
        List<String> roleNames =
                user.getRoles().stream().map(Role::getName).sorted().toList();
        return new UserResponse(user.getId(), user.getEmail(), user.getFullName(), roleNames);
    }
}
