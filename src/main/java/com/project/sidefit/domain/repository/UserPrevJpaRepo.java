package com.project.sidefit.domain.repository;

import com.project.sidefit.domain.entity.UserPrev;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPrevJpaRepo extends JpaRepository<UserPrev, Long> {
    Optional<UserPrev> findByEmailAndEnable(String email, boolean enable);

    boolean existsByEmailAndEnable(String email, boolean enable);
}
