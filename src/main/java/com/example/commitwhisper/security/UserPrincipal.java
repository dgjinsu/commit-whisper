package com.example.commitwhisper.security;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public class UserPrincipal implements UserDetails, OidcUser {

    private final UserPrincipalDto userDto;
    private Map<String, Object> attributes;
    private OidcIdToken idToken;
    private OidcUserInfo userInfo;

    public UserPrincipal(UserPrincipalDto userDto) {
        this.userDto = userDto;
    }

    public UserPrincipal(UserPrincipalDto userDto, Map<String, Object> attributes) {
        this.userDto = userDto;
        this.attributes = attributes;
    }

    public UserPrincipal(UserPrincipalDto userDto, Map<String, Object> attributes, OidcIdToken idToken, OidcUserInfo userInfo) {
        this.userDto = userDto;
        this.attributes = attributes;
        this.idToken = idToken;
        this.userInfo = userInfo;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return userDto.password();
    }

    @Override
    public String getUsername() {
        return userDto.loginId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public Long getId() {
        return userDto.id();
    }

    public String getLoginId() {
        return userDto.loginId();
    }

    // OAuth2User 인터페이스 구현
    @Override
    public Map<String, Object> getAttributes() {
        return attributes != null ? attributes : Collections.emptyMap();
    }

    // OAuth2User.getName()과 UserDetails의 사용자 이름을 통합
    @Override
    public String getName() {
        return userDto.name();
    }

    // OidcUser 인터페이스 구현
    @Override
    public Map<String, Object> getClaims() {
        return attributes != null ? attributes : Collections.emptyMap();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return userInfo;
    }

    @Override
    public OidcIdToken getIdToken() {
        return idToken;
    }
}


