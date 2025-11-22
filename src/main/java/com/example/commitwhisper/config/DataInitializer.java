package com.example.commitwhisper.config;

import com.example.commitwhisper.entity.User;
import com.example.commitwhisper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        String defaultLoginId = "rlawlstn";
        
        if (!userRepository.existsByLoginId(defaultLoginId)) {
            User defaultUser = new User(defaultLoginId, defaultLoginId, "기본 사용자");
            userRepository.save(defaultUser);
            log.info("기본 계정이 생성되었습니다. 아이디: {}, 비밀번호: {}", defaultLoginId, defaultLoginId);
        } else {
            log.info("기본 계정이 이미 존재합니다. 아이디: {}", defaultLoginId);
        }
    }
}

