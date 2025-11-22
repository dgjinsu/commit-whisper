package com.example.commitwhisper.dto;

public record CreateRepoInfoReq(
        String owner,
        String repo,
        String triggerBranch,
        String description
) {
}

