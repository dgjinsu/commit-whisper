package com.example.commitwhisper.controller;

import com.example.commitwhisper.dto.history.GetCommitSummaryHistoryRes;
import com.example.commitwhisper.dto.history.GetHistoryDetailRes;
import com.example.commitwhisper.dto.user.LoginUserRes;
import com.example.commitwhisper.service.CommitSummaryHistoryService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CommitSummaryHistoryController {

    private final CommitSummaryHistoryService historyService;

    @GetMapping("/history")
    public String historyPage(HttpSession session, Model model) {
        LoginUserRes.UserInfo user = (LoginUserRes.UserInfo) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<GetCommitSummaryHistoryRes> histories = historyService.findByUserId(user.id());
        model.addAttribute("user", user);
        model.addAttribute("histories", histories);
        model.addAttribute("historyCount", histories.size());

        return "history";
    }

    @GetMapping("/history/{id}")
    public String historyDetailPage(@PathVariable Long id, HttpSession session, Model model) {
        LoginUserRes.UserInfo user = (LoginUserRes.UserInfo) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        GetCommitSummaryHistoryRes history = historyService.findById(id);
        
        // 본인의 히스토리만 조회 가능하도록 검증
        if (!history.userId().equals(user.id())) {
            return "redirect:/history";
        }

        model.addAttribute("user", user);
        model.addAttribute("history", history);

        return "history-detail";
    }

    @GetMapping("/api/history/{id}")
    public ResponseEntity<GetHistoryDetailRes> getHistoryDetail(@PathVariable Long id, HttpSession session) {
        LoginUserRes.UserInfo user = (LoginUserRes.UserInfo) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        GetCommitSummaryHistoryRes history = historyService.findById(id);
        
        // 본인의 히스토리만 조회 가능하도록 검증
        if (!history.userId().equals(user.id())) {
            return ResponseEntity.status(403).build();
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
    }
}

