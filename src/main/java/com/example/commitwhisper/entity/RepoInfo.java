package com.example.commitwhisper.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "repo_info")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RepoInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "repo_id")
    private Long id;

    @Column(nullable = false)
    private String owner;

    @Column(nullable = false)
    private String repo;

    @Column(nullable = false)
    private String triggerBranch;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "last_whisper_commit_time")
    private LocalDateTime lastWhisperCommitTime;

    public RepoInfo(String owner, String repo, String triggerBranch, String description) {
        this.owner = owner;
        this.repo = repo;
        this.triggerBranch = triggerBranch;
        this.description = description;
        this.lastWhisperCommitTime = null;
    }

    public void updateLastWhisperCommitTime(LocalDateTime commitTime) {
        this.lastWhisperCommitTime = commitTime;
    }
}

