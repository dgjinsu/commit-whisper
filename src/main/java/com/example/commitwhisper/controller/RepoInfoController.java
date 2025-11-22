package com.example.commitwhisper.controller;

import com.example.commitwhisper.dto.CreateRepoInfoReq;
import com.example.commitwhisper.dto.GetRepoInfoRes;
import com.example.commitwhisper.dto.LoginUserRes;
import com.example.commitwhisper.service.RepoInfoService;
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
public class RepoInfoController {

    private final RepoInfoService repoInfoService;

    @GetMapping("/repos")
    public String repoListPage(HttpSession session, Model model) {
        LoginUserRes.UserInfo user = (LoginUserRes.UserInfo) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<GetRepoInfoRes> repos = repoInfoService.findAll();
        model.addAttribute("user", user);
        model.addAttribute("repos", repos);
        model.addAttribute("createReq", new CreateRepoInfoReq(user.id(), "", "", "", ""));
        return "repos";
    }

    @PostMapping("/repos")
    public String createRepo(@ModelAttribute CreateRepoInfoReq createReq, HttpSession session, RedirectAttributes redirectAttributes) {
        LoginUserRes.UserInfo user = (LoginUserRes.UserInfo) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            // 현재 로그인한 유저의 ID로 저장소 생성
            CreateRepoInfoReq reqWithUserId = new CreateRepoInfoReq(
                    user.id(),
                    createReq.owner(),
                    createReq.repo(),
                    createReq.triggerBranch(),
                    createReq.description()
            );
            repoInfoService.create(reqWithUserId);
            redirectAttributes.addFlashAttribute("message", "저장소가 등록되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        redirectAttributes.addFlashAttribute("user", user);
        return "redirect:/repos";
    }
}

