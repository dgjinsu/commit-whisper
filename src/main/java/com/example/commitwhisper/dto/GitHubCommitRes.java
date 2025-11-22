package com.example.commitwhisper.dto;

public record GitHubCommitRes(
        String sha,
        Commit commit
) {
    public record Commit(
            String message,
            Author author
    ) {
    }

    public record Author(
            String name,
            String email,
            String date
    ) {
    }
}

