package com.pdunghh.auth.dto.response;

import java.util.UUID;

import com.pdunghh.auth.entity.User;

public record UserResponse(
                UUID id,
                String username,
                String email,
                String role,
                String avatarUrl) {

        public static UserResponse fromUser(User user) {
                return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole(),
                                user.getAvatarUrl());
        }

}
