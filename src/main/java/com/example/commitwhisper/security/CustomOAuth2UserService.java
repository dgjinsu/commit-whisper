package com.example.commitwhisper.security;

import com.example.commitwhisper.entity.User;
import com.example.commitwhisper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Github 용
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        // GitHub 전용 파싱
        String providerId = extractProviderId(oAuth2User);
        String name = extractName(oAuth2User);
        String email = extractEmail(oAuth2User);
        String loginId = extractLoginId(oAuth2User, email, providerId);

        User user = findOrCreateUser(registrationId, providerId, loginId, name, email);
        UserPrincipalDto userDto = createUserPrincipalDto(user);
        
        return new UserPrincipal(userDto, oAuth2User.getAttributes());
    }

    private String extractProviderId(OAuth2User oAuth2User) {
        Object id = oAuth2User.getAttribute("id");
        return id != null ? id.toString() : oAuth2User.getName();
    }

    private String extractName(OAuth2User oAuth2User) {
        String name = oAuth2User.getAttribute("name");
        // GitHub의 경우 name이 없으면 login 사용
        return name != null ? name : oAuth2User.getAttribute("login");
    }

    private String extractEmail(OAuth2User oAuth2User) {
        return oAuth2User.getAttribute("email");
    }

    private String extractLoginId(OAuth2User oAuth2User, String email, String providerId) {
        // GitHub의 경우 login을 우선 사용
        String login = oAuth2User.getAttribute("login");
        if (login != null) {
            return login;
        }
        return email != null ? email : ("github_" + providerId);
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

