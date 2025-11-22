package com.example.commitwhisper.dto.github;

import java.util.List;

public record GitHubCommitDetailRes(
    Commit commit,
    List<File> files
) {

    public record Commit(
        String message,
        Author author
    ) {

        public record Author(
            String date
        ) {

        }
    }

    public record File(
        String filename,
        String patch
    ) {

    }
}

