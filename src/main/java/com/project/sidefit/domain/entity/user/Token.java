package com.project.sidefit.domain.entity.user;

import com.project.sidefit.domain.entity.time.BaseTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Token extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO PK 로 할지 email 로 할지 >> 현재는 user pk
    @Column(name = "user_id", nullable = false)
    private Long key;

    @Column(nullable = false)
    private String refreshToken;

    public void updateToken(String token) {
        this.refreshToken = token;
    }

    @Builder
    public Token(Long key, String refreshToken) {
        this.key = key;
        this.refreshToken = refreshToken;
    }

    public static Token createToken(Long key, String token) {
        Token refreshToken = new Token();

        refreshToken.key = key;
        refreshToken.refreshToken = token;

        return refreshToken;
    }
}
