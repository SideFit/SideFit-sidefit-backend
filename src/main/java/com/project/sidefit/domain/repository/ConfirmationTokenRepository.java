package com.project.sidefit.domain.repository;

import com.project.sidefit.domain.entity.ConfirmationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, String> {
    Optional<ConfirmationToken> findByTokenAndExpirationAfterAndExpired(String token, LocalDateTime now, boolean expired);

    Optional<ConfirmationToken> findByEmail(String email);
}
