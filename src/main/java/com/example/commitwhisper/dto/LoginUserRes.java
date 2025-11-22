package com.example.commitwhisper.dto;

public record LoginUserRes(
        boolean success,
        String message,
        UserInfo user
) {
    public record UserInfo(
            Long id,
            String loginId,
            String name
    ) {
    }
}

