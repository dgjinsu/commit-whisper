package com.example.commitwhisper.security;

import com.example.commitwhisper.entity.User;
import com.example.commitwhisper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Google Kakao 용
 */
@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String providerId = oidcUser.getSubject();
        String name = oidcUser.getFullName();
        String email = oidcUser.getEmail();
        String loginId = email != null ? email : (registrationId + "_" + providerId);

        User user = userRepository.findByProviderAndProviderId(registrationId, providerId)
            .orElseGet(() -> {
                User newUser = new User(loginId, name, registrationId, providerId, email);
                return userRepository.save(newUser);
            });

        UserPrincipalDto userDto = new UserPrincipalDto(
            user.getId(),
            user.getLoginId(),
            user.getPassword(),
            user.getName()
        );

        return new UserPrincipal(userDto, oidcUser.getClaims(), oidcUser.getIdToken(), oidcUser.getUserInfo());
    }
}

