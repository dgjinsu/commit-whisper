package com.example.commitwhisper.service;

import com.example.commitwhisper.client.GitHubClient;
import com.example.commitwhisper.dto.GetRepoInfoRes;
import com.example.commitwhisper.dto.GitHubCommitDetailRes;
import com.example.commitwhisper.dto.GitHubCommitRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
        
        try {
            List<GitHubCommitRes> commits = githubClient.getCommits(
                    repo.owner(),
                    repo.repo(),
                    repo.triggerBranch()
            );

            // 가장 먼저 발견한 whisper 커밋 찾기
            GitHubCommitRes firstWhisperCommit = null;
            for (GitHubCommitRes commit : commits) {
                String message = commit.commit().message();
                
                if (message != null && message.trim().toLowerCase().startsWith("whisper:")) {
                    firstWhisperCommit = commit;
                    break; // 가장 먼저 발견한 것만 사용
                }
            }

            if (firstWhisperCommit == null) {
                log.debug("저장소 {}/{}에 whisper 커밋이 없습니다.", repo.owner(), repo.repo());
                return;
            }

            // 커밋 시간 파싱
            String commitDateStr = firstWhisperCommit.commit().author().date();
            LocalDateTime commitTime = parseCommitTime(commitDateStr);
            
            // lastWhisperCommitTime과 비교
            LocalDateTime lastTime = repo.lastWhisperCommitTime();
            if (lastTime != null && !commitTime.isAfter(lastTime)) {
                log.debug("저장소 {}/{}의 whisper 커밋이 최신이 아닙니다. 커밋 시간: {}, 마지막 처리 시간: {}", 
                        repo.owner(), repo.repo(), commitTime, lastTime);
                return;
            }

            log.info("새로운 Whisper 커밋 감지 - SHA: {}, 저장소: {}/{}, 커밋 시간: {}", 
                    firstWhisperCommit.sha(), 
                    repo.owner(), 
                    repo.repo(),
                    commitTime);

            // 상세 커밋 정보 조회 및 파싱
            parseCommitDetail(repo, firstWhisperCommit.sha(), commitTime);

        } catch (Exception e) {
            log.error("GitHub API 호출 실패 - 저장소: {}/{}, 브랜치: {}, 오류: {}", 
                    repo.owner(), repo.repo(), repo.triggerBranch(), e.getMessage(), e);
        }
    }

    private void parseCommitDetail(GetRepoInfoRes repo, String sha, LocalDateTime commitTime) {
        try {
            GitHubCommitDetailRes commitDetail = githubClient.getCommitDetail(
                    repo.owner(),
                    repo.repo(),
                    sha
            );

            if (commitDetail.files() != null && !commitDetail.files().isEmpty()) {
                log.info("=== 저장소: {}/{} ===", repo.owner(), repo.repo());
                for (GitHubCommitDetailRes.File file : commitDetail.files()) {
                    log.info("{{");
                    log.info("  fileName: {}", file.filename());
                    log.info("  code: {}", file.patch() != null ? file.patch() : "");
                    log.info("}}");
                }
                log.info("====================");
            }

            // lastWhisperCommitTime 업데이트
            repoInfoService.updateLastWhisperCommitTime(repo.id(), commitTime);
            log.info("저장소 {}/{}의 lastWhisperCommitTime이 업데이트되었습니다: {}", 
                    repo.owner(), repo.repo(), commitTime);

        } catch (Exception e) {
            log.error("커밋 상세 정보 파싱 실패 - 저장소: {}/{}, SHA: {}, 오류: {}", 
                    repo.owner(), repo.repo(), sha, e.getMessage(), e);
        }
    }

    private LocalDateTime parseCommitTime(String dateStr) {
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

