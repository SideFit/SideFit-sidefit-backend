package com.project.sidefit.domain.repository;

import com.project.sidefit.domain.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenJpaRepo extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByKey(Long key);
}
