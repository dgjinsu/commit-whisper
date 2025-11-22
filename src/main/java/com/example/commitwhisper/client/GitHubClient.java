package com.example.commitwhisper.client;

import com.example.commitwhisper.config.GitHubClientConfig;
import com.example.commitwhisper.dto.github.GitHubCommitDetailRes;
import com.example.commitwhisper.dto.github.GitHubCommitRes;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
    name = "github",
    url = "https://api.github.com",
    configuration = GitHubClientConfig.class
)
public interface GitHubClient {

    @GetMapping("/repos/{owner}/{repo}/commits")
    List<GitHubCommitRes> getCommits(
        @PathVariable("owner") String owner,
        @PathVariable("repo") String repo,
        @RequestParam("sha") String branch
    );

    @GetMapping("/repos/{owner}/{repo}/commits/{sha}")
    GitHubCommitDetailRes getCommitDetail(
        @PathVariable("owner") String owner,
        @PathVariable("repo") String repo,
        @PathVariable("sha") String sha
    );
}

