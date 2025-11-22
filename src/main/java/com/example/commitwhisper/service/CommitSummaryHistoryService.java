package com.example.commitwhisper.service;

import com.example.commitwhisper.dto.common.PageResponse;
import com.example.commitwhisper.dto.history.GetCommitSummaryHistoryRes;
import com.example.commitwhisper.dto.history.GetDailyUsageRes;
import com.example.commitwhisper.entity.CommitSummaryHistory;
import com.example.commitwhisper.entity.RepoInfo;
import com.example.commitwhisper.repository.CommitSummaryHistoryRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public PageResponse<GetCommitSummaryHistoryRes> findByUserIdWithPaging(Long userId, int page, int size) {
        return historyRepository.findByUserIdWithPaging(userId, page, size);
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

    @Transactional(readOnly = true)
    public long countTodayByUserId(Long userId) {
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        return historyRepository.countByUserIdAndCreatedAtAfter(userId, todayStart);
    }

    @Transactional(readOnly = true)
    public long countLastWeekByUserId(Long userId) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7).withHour(0).withMinute(0).withSecond(0)
            .withNano(0);
        return historyRepository.countByUserIdAndCreatedAtAfter(userId, sevenDaysAgo);
    }

    @Transactional(readOnly = true)
    public List<GetDailyUsageRes> findDailyUsageByUserId(Long userId, int days) {
        LocalDate startDate = LocalDate.now().minusDays(days - 1);
        List<Object[]> results = historyRepository.findDailyUsageByUserId(userId, startDate);

        // 결과를 Map으로 변환 (날짜 -> 개수)
        Map<LocalDate, Long> usageMap = results.stream()
            .collect(Collectors.toMap(
                row -> {
                    Object dateObj = row[0];
                    if (dateObj instanceof java.sql.Date) {
                        return ((java.sql.Date) dateObj).toLocalDate();
                    } else if (dateObj instanceof java.time.LocalDate) {
                        return (LocalDate) dateObj;
                    } else {
                        return LocalDate.parse(dateObj.toString());
                    }
                },
                row -> ((Number) row[1]).longValue()
            ));

        // 최근 7일간의 모든 날짜에 대해 데이터 생성 (없는 날짜는 0으로)
        List<GetDailyUsageRes> dailyUsage = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            long count = usageMap.getOrDefault(date, 0L);
            dailyUsage.add(new GetDailyUsageRes(date, count));
        }

        return dailyUsage;
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

