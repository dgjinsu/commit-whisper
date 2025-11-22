package com.example.commitwhisper.service;

import com.example.commitwhisper.client.GitHubClient;
import com.example.commitwhisper.dto.GetRepoInfoRes;
import com.example.commitwhisper.dto.GitHubCommitRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommitCheckService {

    private final GitHubClient githubClient;
    private final RepoInfoService repoInfoService;

    public void checkCommits() {
        log.info("커밋 체크 시작");
        
        List<GetRepoInfoRes> repos = repoInfoService.findAll();
        
        if (repos.isEmpty()) {
            log.info("등록된 저장소가 없습니다.");
            return;
        }

        for (GetRepoInfoRes repo : repos) {
            try {
                checkCommitsForRepo(repo);
            } catch (Exception e) {
                log.error("저장소 {} 커밋 체크 중 오류 발생: {}", 
                        repo.owner() + "/" + repo.repo(), e.getMessage(), e);
            }
        }
        
        log.info("커밋 체크 완료");
    }

    private void checkCommitsForRepo(GetRepoInfoRes repo) {
        log.debug("저장소 체크: {}/{} 브랜치: {}", repo.owner(), repo.repo(), repo.triggerBranch());
        System.out.println("test");
        try {
            List<GitHubCommitRes> commits = githubClient.getCommits(
                    repo.owner(),
                    repo.repo(),
                    repo.triggerBranch()
            );

            for (GitHubCommitRes commit : commits) {
                String message = commit.commit().message();
                
                if (message != null && message.trim().toLowerCase().startsWith("whisper:")) {
                    log.info("Whisper 커밋 감지 - SHA: {}, 저장소: {}/{}, 메시지: {}", 
                            commit.sha(), 
                            repo.owner(), 
                            repo.repo(),
                            message);
                }
            }
        } catch (Exception e) {
            log.error("GitHub API 호출 실패 - 저장소: {}/{}, 브랜치: {}, 오류: {}", 
                    repo.owner(), repo.repo(), repo.triggerBranch(), e.getMessage());
        }
    }
}

