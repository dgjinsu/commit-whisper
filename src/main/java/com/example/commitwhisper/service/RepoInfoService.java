package com.example.commitwhisper.service;

import com.example.commitwhisper.dto.CreateRepoInfoReq;
import com.example.commitwhisper.dto.GetRepoInfoRes;
import com.example.commitwhisper.entity.RepoInfo;
import com.example.commitwhisper.entity.User;
import com.example.commitwhisper.repository.RepoInfoRepository;
import com.example.commitwhisper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RepoInfoService {

    private final RepoInfoRepository repoInfoRepository;
    private final UserRepository userRepository;

    @Transactional
    public GetRepoInfoRes create(CreateRepoInfoReq req) {
        if (repoInfoRepository.existsByOwnerAndRepo(req.owner(), req.repo())) {
            throw new IllegalArgumentException("이미 등록된 저장소입니다.");
        }

        User user = userRepository.findById(req.userId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        RepoInfo repoInfo = new RepoInfo(
                user,
                req.owner(),
                req.repo(),
                req.triggerBranch(),
                req.description()
        );

        RepoInfo saved = repoInfoRepository.save(repoInfo);
        return new GetRepoInfoRes(
                saved.getId(),
                saved.getOwner(),
                saved.getRepo(),
                saved.getTriggerBranch(),
                saved.getDescription(),
                saved.getLastWhisperCommitTime()
        );
    }

    public List<GetRepoInfoRes> findAll() {
        return repoInfoRepository.findAllByOrderByIdDesc().stream()
                .map(repo -> new GetRepoInfoRes(
                        repo.getId(),
                        repo.getOwner(),
                        repo.getRepo(),
                        repo.getTriggerBranch(),
                        repo.getDescription(),
                        repo.getLastWhisperCommitTime()
                ))
                .collect(Collectors.toList());
    }

}

