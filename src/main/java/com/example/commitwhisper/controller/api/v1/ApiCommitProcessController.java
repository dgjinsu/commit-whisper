package com.example.commitwhisper.controller.api.v1;

import com.example.commitwhisper.security.UserPrincipal;
import com.example.commitwhisper.service.CommitProcessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/commit-process")
@RequiredArgsConstructor
public class ApiCommitProcessController {

    private final CommitProcessService commitProcessService;

    @PostMapping("/manual")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> manualProcess(
        @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        // 비동기로 처리 시작 (응답은 즉시 반환)
        commitProcessService.processUserRepos(userPrincipal.getId());
        return ResponseEntity.accepted().build();
    }
}

