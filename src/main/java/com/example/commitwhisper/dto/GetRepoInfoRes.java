package com.example.commitwhisper.dto;

import java.time.LocalDateTime;

public record GetRepoInfoRes(
        Long id,
        String owner,
        String repo,
        String triggerBranch,
        String description,
        LocalDateTime lastWhisperCommitTime
) {
}

