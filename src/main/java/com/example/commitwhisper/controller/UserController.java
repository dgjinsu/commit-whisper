package com.example.commitwhisper.controller;

import com.example.commitwhisper.dto.history.GetCommitSummaryHistoryRes;
import com.example.commitwhisper.dto.repo.GetRepoInfoRes;
import com.example.commitwhisper.dto.user.GetUserSettingsRes;
import com.example.commitwhisper.dto.user.LoginUserReq;
import com.example.commitwhisper.dto.user.LoginUserRes;
import com.example.commitwhisper.security.UserPrincipal;
import com.example.commitwhisper.service.CommitSummaryHistoryService;
import com.example.commitwhisper.service.RepoInfoService;
import com.example.commitwhisper.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final RepoInfoService repoInfoService;
    private final CommitSummaryHistoryService historyService;
    private final UserService userService;

    @GetMapping("/login")
    public String loginPage(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/";
        }
        model.addAttribute("loginReq", new LoginUserReq("", ""));
        return "login";
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

        List<GetRepoInfoRes> repos = repoInfoService.findAllByUserId(user.id());
        List<GetCommitSummaryHistoryRes> recentHistories = historyService.findRecentByUserId(user.id(), 5);
        long todayCount = historyService.countTodayByUserId(user.id());
        long lastWeekCount = historyService.countLastWeekByUserId(user.id());

        model.addAttribute("user", user);
        model.addAttribute("repos", repos);
        model.addAttribute("repoCount", repos.size());
        model.addAttribute("recentHistories", recentHistories);
        model.addAttribute("todayCount", todayCount);
        model.addAttribute("lastWeekCount", lastWeekCount);
        return "home";
    }

    @GetMapping("/settings")
    @PreAuthorize("isAuthenticated()")
    public String settings(
        @RequestParam(required = false) String message,
        @RequestParam(required = false) String error,
        Model model,
        @AuthenticationPrincipal(expression = "toUserPrincipal()") UserPrincipal userPrincipal
    ) {
        LoginUserRes.UserInfo user = new LoginUserRes.UserInfo(
            userPrincipal.getId(),
            userPrincipal.getLoginId(),
            userPrincipal.getName()
        );

        GetUserSettingsRes settings = userService.getSettings(user.id());

        model.addAttribute("user", user);
        model.addAttribute("settings", settings);
        model.addAttribute("message", message);
        model.addAttribute("error", error);
        return "settings";
    }
}

