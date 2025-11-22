package com.example.commitwhisper.service;

import com.example.commitwhisper.client.GitHubClient;
import com.example.commitwhisper.dto.github.GitHubCommitDetailRes;
import com.example.commitwhisper.dto.github.GitHubCommitRes;
import com.example.commitwhisper.entity.RepoInfo;
import com.example.commitwhisper.repository.RepoInfoRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommitCheckService {

    private final GitHubClient githubClient;
    private final RepoInfoRepository repoInfoRepository;

    public List<RepoInfo> getAllRepos() {
        return repoInfoRepository.findAllByOrderByIdDesc();
    }

    public Optional<GitHubCommitRes> findWhisperCommit(RepoInfo repo) {
        List<GitHubCommitRes> commits = githubClient.getCommits(
            repo.getOwner(),
            repo.getRepo(),
            repo.getTriggerBranch()
        );

        // 가장 먼저 발견한 whisper 커밋 찾기
        for (GitHubCommitRes commit : commits) {
            String message = commit.commit().message();

            if (message != null && message.trim().toLowerCase().startsWith("whisper:")) {
                log.info("Whisper 커밋 감지 - SHA: {}, 저장소: {}/{}",
                    commit.sha(), repo.getOwner(), repo.getRepo());
                return Optional.of(commit);
            }
        }

        log.debug("저장소 {}/{}에 whisper 커밋이 없습니다.", repo.getOwner(), repo.getRepo());
        return Optional.empty();
    }

    public GitHubCommitDetailRes getCommitDetail(RepoInfo repo, String sha) {
        GitHubCommitDetailRes commitDetail = githubClient.getCommitDetail(
            repo.getOwner(),
            repo.getRepo(),
            sha
        );

        log.info("커밋 상세 정보 조회 완료 - 저장소: {}/{}, SHA: {}",
            repo.getOwner(), repo.getRepo(), sha);
        return commitDetail;
    }


    @Transactional
    public void updateLastWhisperCommitTime(RepoInfo repo, LocalDateTime commitTime) {
        repo.updateLastWhisperCommitTime(commitTime);
        repoInfoRepository.save(repo);
        log.info("저장소 {}/{}의 lastWhisperCommitTime이 업데이트되었습니다: {}",
            repo.getOwner(), repo.getRepo(), commitTime);
    }

    public LocalDateTime parseCommitTime(String dateStr) {
        try {
            // ISO 8601 형식: "2025-11-22T04:25:33Z"
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateStr);
            return zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        } catch (Exception e) {
            log.error("커밋 시간 파싱 실패: {}", dateStr, e);
            return LocalDateTime.now();
        }
    }
}


