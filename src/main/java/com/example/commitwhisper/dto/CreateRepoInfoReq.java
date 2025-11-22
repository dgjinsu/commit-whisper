package com.example.commitwhisper.dto;

public record CreateRepoInfoReq(
        Long userId,
        String owner,
        String repo,
        String triggerBranch,
        String description
) {
}

