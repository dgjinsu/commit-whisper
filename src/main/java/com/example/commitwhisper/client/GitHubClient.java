package com.example.commitwhisper.client;

import com.example.commitwhisper.dto.GitHubCommitRes;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "github", url = "https://api.github.com")
public interface GitHubClient {

    @GetMapping("/repos/{owner}/{repo}/commits")
    List<GitHubCommitRes> getCommits(
            @PathVariable("owner") String owner,
            @PathVariable("repo") String repo,
            @RequestParam("sha") String branch
    );
}

