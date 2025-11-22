package com.example.commitwhisper.repository;

import com.example.commitwhisper.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);
}

