package com.example.commitwhisper.service;

import com.example.commitwhisper.entity.RepoInfo;
import com.example.commitwhisper.repository.RepoInfoRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommitProcessService {

    private final CommitCheckService commitCheckService;
    private final OpenAiService openAiService;
    private final CommitSummaryHistoryService commitSummaryHistoryService;
    private final SlackAlarmService slackAlarmService;
    private final RepoInfoRepository repoInfoRepository;
    @Qualifier("commitProcessExecutor")
    private final Executor commitProcessExecutor;

    /**
     * 사용자의 모든 레포지토리에 대해 커밋 체크 및 처리
     */
    @Transactional(readOnly = true)
    public void processUserRepos(Long userId) {
        log.info("사용자 {}의 레포지토리 수동 처리 시작", userId);

        List<RepoInfo> repos = repoInfoRepository.findAllByUserIdOrderByIdDesc(userId);
        log.info("처리할 레포지토리 수: {}", repos.size());

        if (repos.isEmpty()) {
            log.info("처리할 레포지토리가 없습니다.");
            return;
        }

        // 비동기로 모든 레포지토리 병렬 처리
        List<CompletableFuture<Void>> futures = repos.stream()
            .map(repo -> CompletableFuture.runAsync(() -> processRepo(repo), commitProcessExecutor))
            .toList();

        // 모든 작업 완료 대기
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenRun(() -> log.info("사용자 {}의 모든 레포지토리 처리 완료", userId))
            .exceptionally(ex -> {
                log.error("사용자 {}의 레포지토리 처리 중 오류 발생", userId, ex);
                return null;
            });
    }

    /**
     * 단일 레포지토리 처리 로직
     */
    @Transactional
    public void processRepo(RepoInfo repo) {
        try {
            String repoInfo = String.format("%s/%s", repo.getOwner(), repo.getRepo());
            log.debug("레포지토리 처리 시작: {}", repoInfo);

            // 1. Whisper 커밋 찾기
            commitCheckService.findWhisperCommit(repo)
                .ifPresentOrElse(
                    whisperCommit -> {
                        try {
                            // 2. 커밋 시간 파싱
                            String commitDateStr = whisperCommit.commit().author().date();
                            LocalDateTime commitTime = commitCheckService.parseCommitTime(commitDateStr);

                            log.info("Whisper 커밋 처리 시작 - SHA: {}, 저장소: {}, 커밋 시간: {}",
                                whisperCommit.sha(), repoInfo, commitTime);

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

                            log.info("레포지토리 처리 완료: {}", repoInfo);
                        } catch (Exception e) {
                            log.error("레포지토리 {} 처리 중 오류 발생", repoInfo, e);
                        }
                    },
                    () -> log.debug("Whisper 커밋 없음: {}", repoInfo)
                );
        } catch (Exception e) {
            log.error("레포지토리 {}/{} 처리 중 예외 발생", repo.getOwner(), repo.getRepo(), e);
        }
    }
}

