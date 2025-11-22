package com.example.commitwhisper.controller;

import com.example.commitwhisper.dto.common.PageResponse;
import com.example.commitwhisper.dto.history.GetCommitSummaryHistoryRes;
import com.example.commitwhisper.dto.user.LoginUserRes;
import com.example.commitwhisper.security.UserPrincipal;
import com.example.commitwhisper.service.CommitSummaryHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class CommitSummaryHistoryController {

    private final CommitSummaryHistoryService historyService;

    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    public String historyPage(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        Model model,
        @AuthenticationPrincipal UserPrincipal userPrincipal) {
        LoginUserRes.UserInfo user = new LoginUserRes.UserInfo(
            userPrincipal.getId(),
            userPrincipal.getLoginId(),
            userPrincipal.getName()
        );

        PageResponse<GetCommitSummaryHistoryRes> pageRes = historyService.findByUserIdWithPaging(user.id(), page, size);

        model.addAttribute("user", user);
        model.addAttribute("histories", pageRes.content());
        model.addAttribute("historyCount", pageRes.totalElements());
        model.addAttribute("currentPage", pageRes.page());
        model.addAttribute("pageSize", pageRes.size());
        model.addAttribute("totalPages", pageRes.totalPages());
        model.addAttribute("totalElements", pageRes.totalElements());
        model.addAttribute("hasNext", pageRes.hasNext());
        model.addAttribute("hasPrevious", pageRes.hasPrevious());

        return "history";
    }

    @GetMapping("/history/{id}")
    @PreAuthorize("isAuthenticated()")
    public String historyDetailPage(
        @PathVariable Long id,
        Model model,
        @AuthenticationPrincipal UserPrincipal userPrincipal) {
        LoginUserRes.UserInfo user = new LoginUserRes.UserInfo(
            userPrincipal.getId(),
            userPrincipal.getLoginId(),
            userPrincipal.getName()
        );

        GetCommitSummaryHistoryRes history = historyService.findById(id);

        // 본인의 히스토리만 조회 가능하도록 검증
        if (!history.userId().equals(user.id())) {
            return "redirect:/history";
        }

        model.addAttribute("user", user);
        model.addAttribute("history", history);

        return "history-detail";
    }
}

