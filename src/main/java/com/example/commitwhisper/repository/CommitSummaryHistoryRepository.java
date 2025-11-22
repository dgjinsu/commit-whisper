package com.example.commitwhisper.repository;

import com.example.commitwhisper.entity.CommitSummaryHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface CommitSummaryHistoryRepository extends JpaRepository<CommitSummaryHistory, Long> {
    
    List<CommitSummaryHistory> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    @Query("SELECT COUNT(h) FROM CommitSummaryHistory h WHERE h.user.id = :userId AND h.createdAt >= :startDateTime")
    long countByUserIdAndCreatedAtAfter(@Param("userId") Long userId, @Param("startDateTime") LocalDateTime startDateTime);
    
    @Query(value = "SELECT CAST(h.created_at AS DATE) as date, COUNT(*) as count " +
           "FROM commit_summary_history h " +
           "WHERE h.user_id = :userId AND CAST(h.created_at AS DATE) >= :startDate " +
           "GROUP BY CAST(h.created_at AS DATE) " +
           "ORDER BY CAST(h.created_at AS DATE) ASC", nativeQuery = true)
    List<Object[]> findDailyUsageByUserId(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
}

