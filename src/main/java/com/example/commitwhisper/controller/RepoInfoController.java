package com.example.commitwhisper.controller;

import com.example.commitwhisper.dto.repo.GetRepoInfoRes;
import com.example.commitwhisper.dto.user.LoginUserRes;
import com.example.commitwhisper.security.UserPrincipal;
import com.example.commitwhisper.service.RepoInfoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class RepoInfoController {

    private final RepoInfoService repoInfoService;

    @GetMapping("/repos")
    @PreAuthorize("isAuthenticated()")
    public String repoListPage(
        Model model,
        @AuthenticationPrincipal UserPrincipal userPrincipal) {
        LoginUserRes.UserInfo user = new LoginUserRes.UserInfo(
            userPrincipal.getId(),
            userPrincipal.getLoginId(),
            userPrincipal.getName()
        );

        List<GetRepoInfoRes> repos = repoInfoService.findAllByUserId(user.id());
        model.addAttribute("user", user);
        model.addAttribute("repos", repos);
        return "repos";
    }


    @GetMapping("/repos/{repoId}/edit")
    @PreAuthorize("isAuthenticated()")
    public String editRepoPage(
        @PathVariable("repoId") Long repoId,
        Model model,
        @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            GetRepoInfoRes repo = repoInfoService.findById(repoId, userPrincipal.getId());

            // 본인 소유 확인
            // Note: GetRepoInfoRes에 userId가 없으므로, 서비스에서 확인하거나 별도로 확인 필요
            // 일단 서비스에서 권한 확인하도록 하고, 여기서는 에러 처리만

            LoginUserRes.UserInfo user = new LoginUserRes.UserInfo(
                userPrincipal.getId(),
                userPrincipal.getLoginId(),
                userPrincipal.getName()
            );

            model.addAttribute("user", user);
            model.addAttribute("repo", repo);
            return "repo-edit";
        } catch (IllegalArgumentException e) {
            return "redirect:/repos";
        }
    }

}

