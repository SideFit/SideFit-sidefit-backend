package com.project.sidefit.domain.repository;

import com.project.sidefit.domain.entity.user.UserPrev;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPrevRepository extends JpaRepository<UserPrev, Long> {
    Optional<UserPrev> findByEmailAndEnable(String email, boolean enable);

    boolean existsByEmailAndEnable(String email, boolean enable);

    boolean existsByEmail(String email);
}
