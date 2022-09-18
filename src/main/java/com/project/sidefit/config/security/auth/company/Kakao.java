package com.project.sidefit.config.security.auth.company;


import com.project.sidefit.config.security.auth.OAuth2UserInfo;
import com.project.sidefit.domain.entity.user.Provider;

import java.util.Map;

public class Kakao extends OAuth2UserInfo {

    public Kakao(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return attributes.get("id").toString();
    }

    @Override
    public String getName() {
//        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
//
//        if (properties == null) {
//            return null;
//        }
//
//        return (String) properties.get("nickname");

        Map<String, Object> properties = (Map<String, Object>) attributes.get("kakao_account");
        if (properties == null) {
            return null;
        }

        Map<String, Object> profile = (Map<String, Object>) properties.get("profile");
        if (profile == null) {
            return null;
        }

        return (String) profile.get("nickname");
    }

    @Override
    public String getEmail() {
        Map<String, Object> properties = (Map<String, Object>) attributes.get("kakao_account");
        if (properties == null) {
            return null;
        }
        return (String) properties.get("email");
    }

    @Override
    public String getProvider(){
        return Provider.kakao.toString();
    }
}
