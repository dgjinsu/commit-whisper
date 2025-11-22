package com.example.commitwhisper.repository;

import com.example.commitwhisper.entity.RepoInfo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RepoInfoRepository extends JpaRepository<RepoInfo, Long> {

    @Query("SELECT r FROM RepoInfo r JOIN FETCH r.user WHERE r.user.id = :userId ORDER BY r.id DESC")
    List<RepoInfo> findAllByUserIdOrderByIdDesc(@Param("userId") Long userId);

    @Query("SELECT r FROM RepoInfo r JOIN FETCH r.user ORDER BY r.id DESC")
    List<RepoInfo> findAllByOrderByIdDesc();

    boolean existsByOwnerAndRepo(String owner, String repo);

    Optional<RepoInfo> findByOwnerAndRepo(String owner, String repo);
}

