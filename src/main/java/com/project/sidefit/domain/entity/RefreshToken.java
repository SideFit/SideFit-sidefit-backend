package com.project.sidefit.domain.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO PK 로 할지 email 로 할지 >> 현재는 user pk
    @Column(name = "user_id", nullable = false)
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
