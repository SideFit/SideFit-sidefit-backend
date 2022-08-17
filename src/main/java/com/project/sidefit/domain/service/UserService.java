package com.project.sidefit.domain.service;

import com.project.sidefit.advice.exception.CUserNotFoundException;
import com.project.sidefit.domain.entity.*;
import com.project.sidefit.domain.repository.UserJpaRepo;
import com.project.sidefit.domain.service.dto.UserDetailDto;
import com.project.sidefit.domain.service.dto.UserListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserJpaRepo userJpaRepo;

    // TODO List가 아닌 page? slice?
    public List<UserListDto> findAll() {
        List<User> users = userJpaRepo.findAll();

        return users.stream().map(u -> new UserListDto(u)).collect(Collectors.toList());
    }

    public UserDetailDto findDetail(Long id) {

        User user = userJpaRepo.findById(id).orElseThrow(CUserNotFoundException::new);
        return new UserDetailDto(user);
    }

    @Transactional
    public Long save(User user1) {
        User user = userJpaRepo.findById(user1.getId()).get();
        user.getTags().add(new Tag("tag1"));
        user.getTags().add(new Tag("tag2"));
        user.getFavorites().add(new Favorite("favorite"));
        user.getCurrentStatuses().add(new CurrentStatus("status"));
        user.getTeches().add(new Tech("tech"));
        user.updateMbti(Mbti.INFP);

        return user.getId();
    }
}
