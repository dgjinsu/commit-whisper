package com.example.commitwhisper.controller.api.v1;

import com.example.commitwhisper.dto.repo.CreateRepoInfoReq;
import com.example.commitwhisper.dto.repo.GetRepoInfoRes;
import com.example.commitwhisper.dto.repo.UpdateRepoInfoReq;
import com.example.commitwhisper.security.UserPrincipal;
import com.example.commitwhisper.service.RepoInfoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/repos")
@RequiredArgsConstructor
public class ApiRepoInfoController {

    private final RepoInfoService repoInfoService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<GetRepoInfoRes>> getAllRepos() {
        List<GetRepoInfoRes> repos = repoInfoService.findAll();
        return ResponseEntity.ok(repos);
    }

    @GetMapping("/{repoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GetRepoInfoRes> getRepo(@PathVariable("repoId") Long repoId) {
        try {
            GetRepoInfoRes repo = repoInfoService.findById(repoId);
            return ResponseEntity.ok(repo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createRepo(
        @RequestBody CreateRepoInfoReq createReq,
        @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            CreateRepoInfoReq reqWithUserId = new CreateRepoInfoReq(
                userPrincipal.getId(),
                createReq.owner(),
                createReq.repo(),
                createReq.triggerBranch(),
                createReq.description()
            );
            GetRepoInfoRes created = repoInfoService.create(reqWithUserId);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
        }
    }

    @PutMapping("/{repoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateRepo(
        @PathVariable("repoId") Long repoId,
        @RequestBody UpdateRepoInfoReq updateReq,
        @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            GetRepoInfoRes updated = repoInfoService.update(repoId, userPrincipal.getId(), updateReq);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("권한")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{repoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteRepo(
        @PathVariable("repoId") Long repoId,
        @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            repoInfoService.delete(repoId, userPrincipal.getId());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("권한")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}