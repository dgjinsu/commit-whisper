package com.example.commitwhisper.service;

import com.example.commitwhisper.dto.user.CreateUserReq;
import com.example.commitwhisper.dto.user.GetUserSettingsRes;
import com.example.commitwhisper.dto.user.UpdateSlackWebhookReq;
import com.example.commitwhisper.entity.User;
import com.example.commitwhisper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void signup(CreateUserReq req) {
        if (userRepository.existsByLoginId(req.loginId())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        String encodedPassword = passwordEncoder.encode(req.password());
        User user = new User(req.loginId(), encodedPassword, req.name());
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public GetUserSettingsRes getSettings(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return new GetUserSettingsRes(user.getSlackWebhookUrl());
    }

    public void updateSlackWebhookUrl(Long userId, UpdateSlackWebhookReq req) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.updateSlackWebhookUrl(req.slackWebhookUrl());
    }
}

