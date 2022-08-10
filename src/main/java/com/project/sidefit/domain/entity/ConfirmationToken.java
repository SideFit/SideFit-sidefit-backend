package com.project.sidefit.domain.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConfirmationToken extends BaseTime {

    private static final Long EMAIL_TOKEN_VALID_MINUTE = 5L;

    // UUID
    @Id
    @Column(name = "confirmation_token_id")
    private String id;

    private boolean expired;

    private String email;

    private LocalDateTime expiration;

    public static ConfirmationToken createEmailConfirmationToken(String email) {
        ConfirmationToken confirmationToken = new ConfirmationToken();
        String uuid = UUID.randomUUID().toString();
        confirmationToken.id = uuid;
        confirmationToken.expired = false;
        confirmationToken.email = email;
        confirmationToken.expiration = LocalDateTime.now().plusMinutes(EMAIL_TOKEN_VALID_MINUTE);

        return confirmationToken;
    }

    public void useToken() {
        this.expired = true;
    }
}
