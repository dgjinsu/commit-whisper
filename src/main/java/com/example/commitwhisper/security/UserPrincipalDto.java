package com.example.commitwhisper.security;

public record UserPrincipalDto(
        Long id,
        String loginId,
        String password,
        String name
) {
}

