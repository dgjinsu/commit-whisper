package com.example.commitwhisper.security;

import com.example.commitwhisper.entity.User;
import com.example.commitwhisper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Google Kakao 용
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OidcUser oidcUser = super.loadUser(userRequest);
        
        // OIDC 공통 파싱 (Google, Kakao 등)
        String providerId = oidcUser.getSubject();
        String name = oidcUser.getFullName();
        String email = oidcUser.getEmail();
        String loginId = extractLoginId(email, registrationId, providerId);

        User user = findOrCreateUser(registrationId, providerId, loginId, name, email);
        UserPrincipalDto userDto = createUserPrincipalDto(user);
        
        return new UserPrincipal(userDto, oidcUser.getClaims(), oidcUser.getIdToken(), oidcUser.getUserInfo());
    }

    private String extractLoginId(String email, String registrationId, String providerId) {
        return email != null ? email : (registrationId + "_" + providerId);
    }

    private User findOrCreateUser(String registrationId, String providerId, String loginId, String name, String email) {
        return userRepository.findByProviderAndProviderId(registrationId, providerId)
            .orElseGet(() -> {
                User newUser = new User(loginId, name, registrationId, providerId, email);
                return userRepository.save(newUser);
            });
    }

    private UserPrincipalDto createUserPrincipalDto(User user) {
        return new UserPrincipalDto(
            user.getId(),
            user.getLoginId(),
            user.getPassword(),
            user.getName()
        );
    }
}

