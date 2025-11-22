package com.example.commitwhisper.dto.github;

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
        String date
    ) {

    }
}

