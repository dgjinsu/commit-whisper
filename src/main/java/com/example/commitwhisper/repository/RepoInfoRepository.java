package com.example.commitwhisper.repository;

import com.example.commitwhisper.entity.RepoInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RepoInfoRepository extends JpaRepository<RepoInfo, Long> {
    List<RepoInfo> findAllByOrderByIdDesc();
    
    boolean existsByOwnerAndRepo(String owner, String repo);
    
    Optional<RepoInfo> findByOwnerAndRepo(String owner, String repo);
}

