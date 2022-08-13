package com.project.sidefit.domain.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue
    @Column(name = "refresh_token_id")
    private Long id;

    // TODO PK 로 할지 email 로 할지 >> 현재는 user pk
    @Column(nullable = false)
    private Long key;

    @Column(nullable = false)
    private String token;

    public void updateToken(String token) {
        this.token = token;
    }

    public static RefreshToken createRefreshToken(Long key, String token) {
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.key = key;
        refreshToken.token = token;

        return refreshToken;
    }
}
