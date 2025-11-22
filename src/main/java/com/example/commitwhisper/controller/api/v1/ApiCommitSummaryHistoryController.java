package com.example.commitwhisper.controller.api.v1;

import com.example.commitwhisper.dto.common.PageResponse;
import com.example.commitwhisper.dto.history.GetCommitSummaryHistoryRes;
import com.example.commitwhisper.dto.history.GetDailyUsageRes;
import com.example.commitwhisper.dto.history.GetHistoryDetailRes;
import com.example.commitwhisper.security.UserPrincipal;
import com.example.commitwhisper.service.CommitSummaryHistoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/history")
@RequiredArgsConstructor
public class ApiCommitSummaryHistoryController {

    private final CommitSummaryHistoryService historyService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<GetCommitSummaryHistoryRes>> getHistories(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(historyService.findByUserIdWithPaging(userPrincipal.getId(), page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GetHistoryDetailRes> getHistoryDetail(
        @PathVariable Long id,
        @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        GetCommitSummaryHistoryRes history = historyService.findById(id);

        // 본인의 히스토리만 조회 가능하도록 검증
        if (!history.userId().equals(userPrincipal.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(new GetHistoryDetailRes(
            history.id(),
            history.repoOwner(),
            history.repoName(),
            history.commitSha(),
            history.summary(),
            history.htmlSummary(),
            history.commitDate(),
            history.createdAt()
        ));
    }

    @GetMapping("/recent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<GetCommitSummaryHistoryRes>> getRecentHistories(
        @RequestParam(defaultValue = "5") int limit,
        @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(historyService.findRecentByUserId(userPrincipal.getId(), limit));
    }

    @GetMapping("/daily-usage")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<GetDailyUsageRes>> getDailyUsage(
        @RequestParam(defaultValue = "7") int days,
        @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(historyService.findDailyUsageByUserId(userPrincipal.getId(), days));
    }
}