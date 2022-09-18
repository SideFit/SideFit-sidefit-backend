package com.project.sidefit.domain.service.auth;

import com.project.sidefit.advice.exception.OAuth2AuthenticationProcessingException;
import com.project.sidefit.config.security.auth.OAuth2UserInfo;
import com.project.sidefit.config.security.auth.OAuth2UserInfoFactory;
import com.project.sidefit.config.security.token.UserPrincipal;
import com.project.sidefit.domain.entity.user.Provider;
import com.project.sidefit.domain.entity.user.Role;
import com.project.sidefit.domain.entity.user.User;
import com.project.sidefit.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CustomDefaultOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }

//        catch (Exception e) {
//            DefaultAssert.isAuthentication(e.getMessage());
//        }

//        return null;
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        log.info("registrationId : {}", registrationId);

        Set<String> keys = attributes.keySet();
        for (String key : keys) {
            log.info("key : {}", key);
        }

        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes);
//        DefaultAssert.isAuthentication(!oAuth2UserInfo.getEmail().isEmpty());

        log.info("email: {}", oAuth2UserInfo.getEmail());
        log.info("provider_id : {}", oAuth2UserInfo.getId());

        if(!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
        }

        Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
        User user;
        if(userOptional.isPresent()) {
            user = userOptional.get();

            if(!user.getProvider().equals(Provider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()))) {
                throw new OAuth2AuthenticationProcessingException("Looks like you're signed up with " +
                        user.getProvider() + " account. Please use your " + user.getProvider() +
                        " account to login.");
            }

//            DefaultAssert.isAuthentication(user.getProvider().equals(Provider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId())));
//            user = updateExistingUser(user, oAuth2UserInfo);
            user.updateNickname(oAuth2UserInfo.getName());
        } else {
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
        }

        log.info("providerId : {}", user.getProviderId());

        return UserPrincipal.create(user, oAuth2User.getAttributes());
    }

    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        User user = User.builder()
                .provider(Provider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()))
                .providerId(oAuth2UserInfo.getId())
                .nickname(oAuth2UserInfo.getName())
                .email(oAuth2UserInfo.getEmail())
//                .imageUrl(oAuth2UserInfo.getImageUrl())
                .role(Role.ROLE_USER)
                .build();

        return userRepository.save(user);
    }

    private User updateExistingUser(User user, OAuth2UserInfo oAuth2UserInfo) {

        user.updateNickname(oAuth2UserInfo.getName());
//        user.updateImageUrl(oAuth2UserInfo.getImageUrl());

        return userRepository.save(user);
    }
}
