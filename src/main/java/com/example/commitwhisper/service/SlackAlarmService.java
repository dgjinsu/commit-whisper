package com.example.commitwhisper.service;

import com.example.commitwhisper.entity.RepoInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlackAlarmService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public void sendCommitSummary(RepoInfo repo, String commitSha, String summary, String commitTime, String slackWebhookUrl) {
        if (slackWebhookUrl == null || slackWebhookUrl.isBlank()) {
            log.debug("Slack webhook URL이 설정되지 않아 알림을 전송하지 않습니다 - 저장소: {}/{}", repo.getOwner(), repo.getRepo());
            return;
        }

        try {
            String message = buildSlackMessage(repo, commitSha, summary, commitTime);
            sendToSlack(slackWebhookUrl, message);
            log.info("Slack 알림 전송 완료 - 저장소: {}/{}, SHA: {}", repo.getOwner(), repo.getRepo(), commitSha);
        } catch (Exception e) {
            log.error("Slack 알림 전송 실패 - 저장소: {}/{}, SHA: {}", repo.getOwner(), repo.getRepo(), commitSha, e);
        }
    }

    private String buildSlackMessage(RepoInfo repo, String commitSha, String summary, String commitTime) {
        StringBuilder sb = new StringBuilder();
        sb.append("🚀 *커밋 요약 알림*\n\n");
        sb.append("*저장소:* ").append(repo.getOwner()).append("/").append(repo.getRepo()).append("\n");
        sb.append("*커밋 SHA:* `").append(commitSha).append("`\n");
        sb.append("*커밋 시간:* ").append(commitTime).append("\n");
        sb.append("*요약:*\n").append(summary);
        return sb.toString();
    }

    private void sendToSlack(String webhookUrl, String message) throws Exception {
        Map<String, String> payload = new HashMap<>();
        payload.put("text", message);

        String jsonPayload = objectMapper.writeValueAsString(payload);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);

        restTemplate.postForEntity(webhookUrl, request, String.class);
    }
}

