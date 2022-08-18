package com.project.sidefit.domain.service.security;

import com.project.sidefit.advice.exception.CUserNotFoundException;
import com.project.sidefit.domain.repository.UserJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserJpaRepo userJpaRepo;

    @Override
    public UserDetails loadUserByUsername(String userPk) throws UsernameNotFoundException {

        return userJpaRepo.findById(Long.parseLong(userPk)).orElseThrow(CUserNotFoundException::new);
    }
}
