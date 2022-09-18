package com.project.sidefit.domain.entity.user;

import lombok.Getter;

public enum Role {
    ROLE_GUEST("ROLE_GUEST"),
    ROLE_USER("ROLE_USER");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
