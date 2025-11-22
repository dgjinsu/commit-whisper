package com.example.commitwhisper.service;

import com.example.commitwhisper.dto.history.GetCommitSummaryHistoryRes;
import com.example.commitwhisper.entity.CommitSummaryHistory;
import com.example.commitwhisper.entity.RepoInfo;
import com.example.commitwhisper.repository.CommitSummaryHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommitSummaryHistoryService {

    private final CommitSummaryHistoryRepository historyRepository;

    @Transactional(readOnly = true)
    public List<GetCommitSummaryHistoryRes> findByUserId(Long userId) {
        return historyRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GetCommitSummaryHistoryRes> findRecentByUserId(Long userId, int limit) {
        return historyRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .limit(limit)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GetCommitSummaryHistoryRes findById(Long historyId) {
        CommitSummaryHistory history = historyRepository.findById(historyId)
                .orElseThrow(() -> new IllegalArgumentException("히스토리를 찾을 수 없습니다."));
        return toResponse(history);
    }

    @Transactional
    public void saveCommitHistory(RepoInfo repo, String sha, String summary, LocalDateTime commitTime) {
        CommitSummaryHistory history = new CommitSummaryHistory(
                repo.getUser(),
                repo,
                sha,
                summary,
                commitTime
        );
        
        historyRepository.save(history);
        log.info("커밋 요약 히스토리 저장 완료 - 저장소: {}/{}, SHA: {}", 
                repo.getOwner(), repo.getRepo(), sha);
    }

    private GetCommitSummaryHistoryRes toResponse(CommitSummaryHistory history) {
        return new GetCommitSummaryHistoryRes(
                history.getId(),
                history.getUser().getId(),
                history.getUser().getName(),
                history.getRepoInfo().getId(),
                history.getRepoInfo().getOwner(),
                history.getRepoInfo().getRepo(),
                history.getCommitSha(),
                history.getSummary(),
                history.getCommitDate(),
                history.getCreatedAt()
        );
    }
}

