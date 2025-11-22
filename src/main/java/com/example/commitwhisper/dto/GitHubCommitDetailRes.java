package com.example.commitwhisper.dto;

import java.util.List;

public record GitHubCommitDetailRes(
        List<File> files
) {

    public record File(
            String filename,
            String patch
    ) {
    }
}

