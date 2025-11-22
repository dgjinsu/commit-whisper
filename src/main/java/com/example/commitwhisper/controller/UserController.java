package com.example.commitwhisper.controller;

import com.example.commitwhisper.dto.GetCommitSummaryHistoryRes;
import com.example.commitwhisper.dto.GetRepoInfoRes;
import com.example.commitwhisper.dto.CreateUserReq;
import com.example.commitwhisper.dto.LoginUserReq;
import com.example.commitwhisper.dto.LoginUserRes;
import com.example.commitwhisper.service.CommitSummaryHistoryService;
import com.example.commitwhisper.service.RepoInfoService;
import com.example.commitwhisper.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final RepoInfoService repoInfoService;
    private final CommitSummaryHistoryService historyService;

    @GetMapping("/login")
    public String loginPage(HttpSession session, Model model) {
        if (session.getAttribute("user") != null) {
            return "redirect:/";
        }
        model.addAttribute("loginReq", new LoginUserReq("", ""));
        return "login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute LoginUserReq loginReq, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            LoginUserRes result = userService.login(loginReq);
            session.setAttribute("user", result.user());
            redirectAttributes.addFlashAttribute("message", result.message());
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/signup")
    public String signupPage(Model model) {
        model.addAttribute("signupReq", new CreateUserReq("", "", ""));
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@ModelAttribute CreateUserReq signupReq, RedirectAttributes redirectAttributes) {
        try {
            userService.signup(signupReq);
            redirectAttributes.addFlashAttribute("message", "회원가입이 완료되었습니다. 로그인해주세요.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/signup";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        LoginUserRes.UserInfo user = (LoginUserRes.UserInfo) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        List<GetRepoInfoRes> repos = repoInfoService.findAll();
        List<GetCommitSummaryHistoryRes> recentHistories = historyService.findRecentByUserId(user.id(), 5);
        
        model.addAttribute("user", user);
        model.addAttribute("repos", repos);
        model.addAttribute("repoCount", repos.size());
        model.addAttribute("recentHistories", recentHistories);
        return "home";
    }
}

