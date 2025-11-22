package com.example.commitwhisper.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User {

    /**
     * 사용자 고유 ID
     */
    @Id
    @GeneratedValue
    @Column(name = "user_id")
    private Long id;

    /**
     * 로그인 ID (고유값, 로컬/소셜 로그인 공통 사용)
     */
    @Column(nullable = false, unique = true)
    private String loginId;

    /**
     * 비밀번호 (로컬 로그인 사용자만 사용, OAuth2 사용자는 null)
     */
    private String password;

    /**
     * 사용자 이름
     */
    private String name;

    /**
     * Slack 웹훅 URL (선택사항, 알림 발송용)
     */
    @Column(name = "slack_webhook_url", columnDefinition = "TEXT")
    private String slackWebhookUrl;

    /**
     * 인증 제공자 (local, google, github, kakao 등)
     */
    @Column(name = "provider")
    private String provider;

    /**
     * 인증 제공자에서 발급한 사용자 ID
     */
    @Column(name = "provider_id")
    private String providerId;

    /**
     * 이메일 주소 (OAuth2 사용자의 경우 제공자에서 받은 이메일)
     */
    @Column(name = "email")
    private String email;

    public User(String loginId, String password, String name) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.slackWebhookUrl = null;
        this.provider = "local";
        this.providerId = null;
        this.email = null;
    }

    public User(String loginId, String name, String provider, String providerId, String email) {
        this.loginId = loginId;
        this.password = null; // OAuth2 사용자는 password 없음
        this.name = name;
        this.slackWebhookUrl = null;
        this.provider = provider;
        this.providerId = providerId;
        this.email = email;
    }

    public void updateSlackWebhookUrl(String slackWebhookUrl) {
        this.slackWebhookUrl = slackWebhookUrl;
    }
}

