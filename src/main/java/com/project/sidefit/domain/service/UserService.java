package com.project.sidefit.domain.service;

import com.project.sidefit.domain.entity.*;
import com.project.sidefit.domain.repository.UserJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserJpaRepo userJpaRepo;

    public List<User> findAll() {
        return userJpaRepo.findAll();
    }


}
