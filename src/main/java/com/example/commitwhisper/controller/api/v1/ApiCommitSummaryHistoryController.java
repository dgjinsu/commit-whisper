package com.example.commitwhisper.controller.api.v1;

import com.example.commitwhisper.dto.history.GetCommitSummaryHistoryRes;
import com.example.commitwhisper.dto.history.GetHistoryDetailRes;
import com.example.commitwhisper.security.UserPrincipal;
import com.example.commitwhisper.service.CommitSummaryHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/history")
@RequiredArgsConstructor
public class ApiCommitSummaryHistoryController {

    private final CommitSummaryHistoryService historyService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<GetCommitSummaryHistoryRes>> getHistories(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<GetCommitSummaryHistoryRes> histories = historyService.findByUserId(userPrincipal.getId());
        return ResponseEntity.ok(histories);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GetHistoryDetailRes> getHistoryDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            GetCommitSummaryHistoryRes history = historyService.findById(id);

            // 본인의 히스토리만 조회 가능하도록 검증
            if (!history.userId().equals(userPrincipal.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            GetHistoryDetailRes response = new GetHistoryDetailRes(
                    history.id(),
                    history.repoOwner(),
                    history.repoName(),
                    history.commitSha(),
                    history.summary(),
                    history.htmlSummary(),
                    history.commitDate(),
                    history.createdAt()
            );

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/recent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<GetCommitSummaryHistoryRes>> getRecentHistories(
            @RequestParam(defaultValue = "5") int limit,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<GetCommitSummaryHistoryRes> histories = historyService.findRecentByUserId(userPrincipal.getId(), limit);
        return ResponseEntity.ok(histories);
    }
}