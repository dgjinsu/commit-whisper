package com.example.commitwhisper.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "commit_summary_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommitSummaryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repo_id", nullable = false)
    private RepoInfo repoInfo;

    @Column(name = "commit_sha", nullable = false, length = 40)
    private String commitSha;

    @Column(name = "summary", columnDefinition = "TEXT", nullable = false)
    private String summary;

    @Column(name = "commit_date", nullable = false)
    private LocalDateTime commitDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public CommitSummaryHistory(User user, RepoInfo repoInfo, String commitSha, String summary, LocalDateTime commitDate) {
        this.user = user;
        this.repoInfo = repoInfo;
        this.commitSha = commitSha;
        this.summary = summary;
        this.commitDate = commitDate;
        this.createdAt = LocalDateTime.now();
    }
}

