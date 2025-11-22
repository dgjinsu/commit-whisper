package com.example.commitwhisper.service;

import com.example.commitwhisper.dto.CreateUserReq;
import com.example.commitwhisper.dto.LoginUserReq;
import com.example.commitwhisper.dto.LoginUserRes;
import com.example.commitwhisper.entity.User;
import com.example.commitwhisper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public void signup(CreateUserReq req) {
        if (userRepository.existsByLoginId(req.loginId())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        User user = new User(req.loginId(), req.password(), req.name());
        userRepository.save(user);
    }

    public LoginUserRes login(LoginUserReq req) {
        User user = userRepository.findByLoginId(req.loginId())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다."));

        if (!user.getPassword().equals(req.password())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        return new LoginUserRes(
                true,
                "로그인 성공",
                new LoginUserRes.UserInfo(
                        user.getId(),
                        user.getLoginId(),
                        user.getName()
                )
        );
    }
}

