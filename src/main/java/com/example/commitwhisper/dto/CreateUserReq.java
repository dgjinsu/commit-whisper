package com.example.commitwhisper.dto;

public record CreateUserReq(
        String loginId,
        String password,
        String name
) {
}

