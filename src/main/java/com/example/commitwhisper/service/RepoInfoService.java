package com.example.commitwhisper.service;

import com.example.commitwhisper.dto.repo.CreateRepoInfoReq;
import com.example.commitwhisper.dto.repo.GetRepoInfoRes;
import com.example.commitwhisper.dto.repo.UpdateRepoInfoReq;
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

    @Transactional
    public GetRepoInfoRes update(Long repoId, Long userId, UpdateRepoInfoReq req) {
        RepoInfo repoInfo = repoInfoRepository.findById(repoId)
                .orElseThrow(() -> new IllegalArgumentException("저장소를 찾을 수 없습니다."));

        // 본인 소유 확인
        if (!repoInfo.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("저장소를 수정할 권한이 없습니다.");
        }

        // owner와 repo가 변경되는 경우 중복 확인
        if (!repoInfo.getOwner().equals(req.owner()) || !repoInfo.getRepo().equals(req.repo())) {
            repoInfoRepository.findByOwnerAndRepo(req.owner(), req.repo())
                    .ifPresent(existingRepo -> {
                        // 다른 저장소인 경우에만 중복 에러
                        if (!existingRepo.getId().equals(repoId)) {
                            throw new IllegalArgumentException("이미 등록된 저장소입니다.");
                        }
                    });
        }

        repoInfo.update(req.owner(), req.repo(), req.triggerBranch(), req.description());
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

    @Transactional
    public void delete(Long repoId, Long userId) {
        RepoInfo repoInfo = repoInfoRepository.findById(repoId)
                .orElseThrow(() -> new IllegalArgumentException("저장소를 찾을 수 없습니다."));

        // 본인 소유 확인
        if (!repoInfo.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("저장소를 삭제할 권한이 없습니다.");
        }

        repoInfoRepository.delete(repoInfo);
    }

    public GetRepoInfoRes findById(Long repoId) {
        RepoInfo repoInfo = repoInfoRepository.findById(repoId)
                .orElseThrow(() -> new IllegalArgumentException("저장소를 찾을 수 없습니다."));

        return new GetRepoInfoRes(
                repoInfo.getId(),
                repoInfo.getOwner(),
                repoInfo.getRepo(),
                repoInfo.getTriggerBranch(),
                repoInfo.getDescription(),
                repoInfo.getLastWhisperCommitTime()
        );
    }

}

