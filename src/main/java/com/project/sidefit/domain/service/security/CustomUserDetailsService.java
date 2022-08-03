package com.project.sidefit.domain.service.security;

import com.project.sidefit.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String userPk) throws UsernameNotFoundException {
        // 엔티티 조회로 변경

        List<String> roles = new ArrayList<>();
        roles.add("ROLE_USER");

        return User.createUser(Long.parseLong(userPk), "test@gmail.com", "test1234", roles);
    }
}
