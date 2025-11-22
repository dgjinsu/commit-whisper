package com.example.commitwhisper.dto;

public record GetRepoInfoRes(
        Long id,
        String owner,
        String repo,
        String triggerBranch,
        String description
) {
}

