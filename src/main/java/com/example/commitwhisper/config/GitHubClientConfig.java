package com.example.commitwhisper.config;

import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GitHubClientConfig {

    @Value("${github.token:}")
    private String githubToken;

    @Bean
    public RequestInterceptor githubRequestInterceptor() {
        return requestTemplate -> {
            if (githubToken != null && !githubToken.isBlank()) {
                requestTemplate.header("Authorization", "token " + githubToken);
            }
        };
    }
}

