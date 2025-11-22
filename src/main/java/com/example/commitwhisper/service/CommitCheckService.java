package com.example.commitwhisper.service;

import com.example.commitwhisper.client.GitHubClient;
import com.example.commitwhisper.entity.CommitSummaryHistory;
import com.example.commitwhisper.entity.RepoInfo;
import com.example.commitwhisper.dto.github.GitHubCommitDetailRes;
import com.example.commitwhisper.dto.github.GitHubCommitRes;
import com.example.commitwhisper.repository.CommitSummaryHistoryRepository;
import com.example.commitwhisper.repository.RepoInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommitCheckService {

    private final GitHubClient githubClient;
    private final RepoInfoRepository repoInfoRepository;
    private final CommitSummaryHistoryRepository historyRepository;
    private final OpenAiService openAiService;

    public void checkCommits() {
        log.info("커밋 체크 시작");
        
        List<RepoInfo> repos = repoInfoRepository.findAllByOrderByIdDesc();
        
        if (repos.isEmpty()) {
            log.info("등록된 저장소가 없습니다.");
            return;
        }

        for (RepoInfo repo : repos) {
            try {
                checkCommitsForRepo(repo);
            } catch (Exception e) {
                log.error("저장소 {} 커밋 체크 중 오류 발생: {}", 
                        repo.getOwner() + "/" + repo.getRepo(), e.getMessage(), e);
            }
        }
        
        log.info("커밋 체크 완료");
    }

    private void checkCommitsForRepo(RepoInfo repo) {
        log.debug("저장소 체크: {}/{} 브랜치: {}", repo.getOwner(), repo.getRepo(), repo.getTriggerBranch());
        
        try {
            List<GitHubCommitRes> commits = githubClient.getCommits(
                    repo.getOwner(),
                    repo.getRepo(),
                    repo.getTriggerBranch()
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
                log.debug("저장소 {}/{}에 whisper 커밋이 없습니다.", repo.getOwner(), repo.getRepo());
                return;
            }

            // 커밋 시간 파싱
            String commitDateStr = firstWhisperCommit.commit().author().date();
            LocalDateTime commitTime = parseCommitTime(commitDateStr);
            
            // lastWhisperCommitTime과 비교
            LocalDateTime lastTime = repo.getLastWhisperCommitTime();
            if (lastTime != null && !commitTime.isAfter(lastTime)) {
                log.debug("저장소 {}/{}의 whisper 커밋이 최신이 아닙니다. 커밋 시간: {}, 마지막 처리 시간: {}", 
                        repo.getOwner(), repo.getRepo(), commitTime, lastTime);
                return;
            }

            log.info("새로운 Whisper 커밋 감지 - SHA: {}, 저장소: {}/{}, 커밋 시간: {}", 
                    firstWhisperCommit.sha(), 
                    repo.getOwner(), 
                    repo.getRepo(),
                    commitTime);

            // 상세 커밋 정보 조회 및 파싱
            parseCommitDetail(repo, firstWhisperCommit.sha(), commitTime);

        } catch (Exception e) {
            log.error("GitHub API 호출 실패 - 저장소: {}/{}, 브랜치: {}, 오류: {}", 
                    repo.getOwner(), repo.getRepo(), repo.getTriggerBranch(), e.getMessage(), e);
        }
    }

    @Transactional
    private void parseCommitDetail(RepoInfo repo, String sha, LocalDateTime commitTime) {
        try {
            GitHubCommitDetailRes commitDetail = githubClient.getCommitDetail(
                    repo.getOwner(),
                    repo.getRepo(),
                    sha
            );

            log.info("커밋 상세 정보 조회 완료 - 저장소: {}/{}, SHA: {}", 
                    repo.getOwner(), repo.getRepo(), sha);

            // OpenAI를 통한 커밋 요약 생성 및 히스토리 저장
            try {
                String summary = openAiService.summarizeCommit(commitDetail);
                log.info("=== 커밋 요약 (저장소: {}/{}) ===", repo.getOwner(), repo.getRepo());
                log.info("\n{}", summary);
                log.info("=====================================");

                // 히스토리 저장
                saveHistory(repo, sha, summary, commitTime);
            } catch (Exception e) {
                log.error("LLM 요약 생성 실패 - 저장소: {}/{}, SHA: {}, 오류: {}", 
                        repo.getOwner(), repo.getRepo(), sha, e.getMessage(), e);
            }

            // lastWhisperCommitTime 업데이트
            repo.updateLastWhisperCommitTime(commitTime);
            repoInfoRepository.save(repo);
            log.info("저장소 {}/{}의 lastWhisperCommitTime이 업데이트되었습니다: {}", 
                    repo.getOwner(), repo.getRepo(), commitTime);

        } catch (Exception e) {
            log.error("커밋 상세 정보 파싱 실패 - 저장소: {}/{}, SHA: {}, 오류: {}", 
                    repo.getOwner(), repo.getRepo(), sha, e.getMessage(), e);
        }
    }

    @Transactional
    private void saveHistory(RepoInfo repo, String sha, String summary, LocalDateTime commitTime) {
        try {
            CommitSummaryHistory history = new CommitSummaryHistory(
                    repo.getUser(),
                    repo,
                    sha,
                    summary,
                    commitTime
            );
            
            historyRepository.save(history);
            log.info("커밋 요약 히스토리 저장 완료 - 저장소: {}/{}, SHA: {}", 
                    repo.getOwner(), repo.getRepo(), sha);
        } catch (Exception e) {
            log.error("히스토리 저장 실패 - 저장소: {}/{}, SHA: {}, 오류: {}", 
                    repo.getOwner(), repo.getRepo(), sha, e.getMessage(), e);
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

