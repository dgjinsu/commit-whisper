package com.example.commitwhisper.scheduler;

import com.example.commitwhisper.entity.RepoInfo;
import com.example.commitwhisper.service.CommitCheckService;
import com.example.commitwhisper.service.CommitSummaryHistoryService;
import com.example.commitwhisper.service.OpenAiService;
import com.example.commitwhisper.service.SlackAlarmService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommitScheduler {

    private final CommitCheckService commitCheckService;
    private final OpenAiService openAiService;
    private final CommitSummaryHistoryService commitSummaryHistoryService;
    private final SlackAlarmService slackAlarmService;

    @Scheduled(fixedRate = 600000) // 10분마다 실행
    public void checkCommits() {
        log.info("스케줄러 실행: 커밋 체크 시작");

        List<RepoInfo> repos = commitCheckService.getAllRepos();

        for (RepoInfo repo : repos) {
            processRepo(repo);
        }
    }

    private void processRepo(RepoInfo repo) {
        // 1. Whisper 커밋 찾기
        commitCheckService.findWhisperCommit(repo)
            .ifPresent(whisperCommit -> {
                // 2. 커밋 시간 파싱
                String commitDateStr = whisperCommit.commit().author().date();
                LocalDateTime commitTime = commitCheckService.parseCommitTime(commitDateStr);

                log.info("Whisper 커밋 처리 시작 - SHA: {}, 저장소: {}/{}, 커밋 시간: {}",
                    whisperCommit.sha(), repo.getOwner(), repo.getRepo(), commitTime);

                // 3. 커밋 상세 정보 조회
                var commitDetail = commitCheckService.getCommitDetail(repo, whisperCommit.sha());

                // 4. LLM 요약 생성
                String summary = openAiService.summarizeCommit(commitDetail);

                // 5. 히스토리 저장
                commitSummaryHistoryService.saveCommitHistory(
                    repo,
                    whisperCommit.sha(),
                    summary,
                    commitTime
                );

                // 6. Slack 알림 전송 (사용자가 설정한 webhook URL이 있을 경우에만)
                String slackWebhookUrl = repo.getUser().getSlackWebhookUrl();
                if (slackWebhookUrl != null && !slackWebhookUrl.isBlank()) {
                    slackAlarmService.sendCommitSummary(
                        repo,
                        whisperCommit.sha(),
                        summary,
                        commitTime.toString(),
                        slackWebhookUrl
                    );
                }

                // 7. lastWhisperCommitTime 업데이트
                commitCheckService.updateLastWhisperCommitTime(repo, commitTime);
            });
    }
}

