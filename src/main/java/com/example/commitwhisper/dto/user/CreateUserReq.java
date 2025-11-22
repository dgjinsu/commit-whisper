package com.example.commitwhisper.dto.user;

public record CreateUserReq(
        String loginId,
        String password,
        String name
) {
}

