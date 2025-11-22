package com.example.commitwhisper.dto.history;

import java.time.LocalDateTime;

public record GetHistoryDetailRes(
        Long id,
        String repoOwner,
        String repoName,
        String commitSha,
        String summary,
        String htmlSummary,
        LocalDateTime commitDate,
        LocalDateTime createdAt
) {
}

