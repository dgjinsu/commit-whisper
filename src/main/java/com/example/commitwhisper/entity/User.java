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

    @Id
    @GeneratedValue
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String loginId;

    private String password;

    private String name;

    @Column(name = "slack_webhook_url", columnDefinition = "TEXT")
    private String slackWebhookUrl;

    @Column(name = "provider")
    private String provider;

    @Column(name = "provider_id")
    private String providerId;

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

