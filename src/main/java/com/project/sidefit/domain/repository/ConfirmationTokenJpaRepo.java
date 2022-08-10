package com.project.sidefit.domain.repository;

import com.project.sidefit.domain.entity.ConfirmationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ConfirmationTokenJpaRepo extends JpaRepository<ConfirmationToken, String> {
    Optional<ConfirmationToken> findByIdAndExpirationAfterAndExpired(String id, LocalDateTime now, boolean expired);
}
