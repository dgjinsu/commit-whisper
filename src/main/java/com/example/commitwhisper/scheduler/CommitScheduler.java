package com.example.commitwhisper.scheduler;

import com.example.commitwhisper.entity.RepoInfo;
import com.example.commitwhisper.service.CommitCheckService;
import com.example.commitwhisper.service.CommitProcessService;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CommitScheduler {

    private final CommitCheckService commitCheckService;
    private final CommitProcessService commitProcessService;
    @Qualifier("commitProcessExecutor")
    private final Executor commitProcessExecutor;

    @Scheduled(fixedRate = 600000) // 10분마다 실행
    public void checkCommits() {
        log.info("스케줄러 실행: 커밋 체크 시작");

        List<RepoInfo> repos = commitCheckService.getAllRepos();
        log.info("처리할 레포지토리 수: {}", repos.size());

        // 비동기로 모든 레포지토리 병렬 처리
        List<CompletableFuture<Void>> futures = repos.stream()
            .map(repo -> CompletableFuture.runAsync(
                () -> commitProcessService.processRepo(repo), 
                commitProcessExecutor
            ))
            .toList();

        // 모든 작업 완료 대기
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenRun(() -> log.info("모든 레포지토리 처리 완료"))
            .exceptionally(ex -> {
                log.error("레포지토리 처리 중 오류 발생", ex);
                return null;
            });
    }
}

