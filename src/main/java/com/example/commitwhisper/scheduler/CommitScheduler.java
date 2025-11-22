package com.example.commitwhisper.scheduler;

import com.example.commitwhisper.service.CommitCheckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommitScheduler {

    private final CommitCheckService commitCheckService;

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void checkCommits() {
        log.info("스케줄러 실행: 커밋 체크 시작");
        commitCheckService.checkCommits();
    }
}

