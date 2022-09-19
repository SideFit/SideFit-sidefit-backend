package com.project.sidefit.domain.repository.user;

import com.project.sidefit.domain.entity.user.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByKey(Long key);
    Optional<Token> findByRefreshToken(String refreshToken);
}
