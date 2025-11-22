package com.example.commitwhisper.repository;

import com.example.commitwhisper.entity.CommitSummaryHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommitSummaryHistoryRepository extends JpaRepository<CommitSummaryHistory, Long> {
    
    List<CommitSummaryHistory> findByUserIdOrderByCreatedAtDesc(Long userId);
}

