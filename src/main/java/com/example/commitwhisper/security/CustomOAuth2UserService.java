package com.example.commitwhisper.security;

import com.example.commitwhisper.entity.User;
import com.example.commitwhisper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String providerId = getProviderId(oAuth2User, registrationId);
        String name = getName(oAuth2User, registrationId);
        String email = getEmail(oAuth2User, registrationId);
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

        return new UserPrincipal(userDto, oAuth2User.getAttributes());
    }

    private String getProviderId(OAuth2User oAuth2User, String registrationId) {
        if ("google".equals(registrationId)) {
            return oAuth2User.getAttribute("sub").toString();
        }
        return oAuth2User.getName();
    }

    private String getName(OAuth2User oAuth2User, String registrationId) {
        if ("google".equals(registrationId)) {
            return oAuth2User.getAttribute("name");
        }
        return oAuth2User.getAttribute("name");
    }

    private String getEmail(OAuth2User oAuth2User, String registrationId) {
        if ("google".equals(registrationId)) {
            return oAuth2User.getAttribute("email");
        }
        return oAuth2User.getAttribute("email");
    }
}

