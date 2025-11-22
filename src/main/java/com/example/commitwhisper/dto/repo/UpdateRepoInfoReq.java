package com.example.commitwhisper.dto.repo;

public record UpdateRepoInfoReq(
        String owner,
        String repo,
        String triggerBranch,
        String description
) {
}

