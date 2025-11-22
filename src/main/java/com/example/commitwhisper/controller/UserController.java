package com.example.commitwhisper.controller;

import com.example.commitwhisper.dto.history.GetCommitSummaryHistoryRes;
import com.example.commitwhisper.dto.repo.GetRepoInfoRes;
import com.example.commitwhisper.dto.user.LoginUserReq;
import com.example.commitwhisper.dto.user.LoginUserRes;
import com.example.commitwhisper.security.UserPrincipal;
import com.example.commitwhisper.service.CommitSummaryHistoryService;
import com.example.commitwhisper.service.RepoInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final RepoInfoService repoInfoService;
    private final CommitSummaryHistoryService historyService;

    @GetMapping("/login")
    public String loginPage(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/";
        }
        model.addAttribute("loginReq", new LoginUserReq("", ""));
        return "login";
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    @GetMapping("/")
    @PreAuthorize("isAuthenticated()")
    public String home(
            Model model,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        LoginUserRes.UserInfo user = new LoginUserRes.UserInfo(
                userPrincipal.getId(),
                userPrincipal.getLoginId(),
                userPrincipal.getName()
        );

        List<GetRepoInfoRes> repos = repoInfoService.findAll();
        List<GetCommitSummaryHistoryRes> recentHistories = historyService.findRecentByUserId(user.id(), 5);

        model.addAttribute("user", user);
        model.addAttribute("repos", repos);
        model.addAttribute("repoCount", repos.size());
        model.addAttribute("recentHistories", recentHistories);
        return "home";
    }
}

