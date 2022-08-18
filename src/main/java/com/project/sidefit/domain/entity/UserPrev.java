package com.project.sidefit.domain.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserPrev {

    @Id
    @GeneratedValue
    private Long id;
    private String email;
    private String password;
    private boolean enable;

    public static UserPrev createUserPrev(String email, String password) {
        UserPrev userPrev = new UserPrev();
        userPrev.email = email;
        userPrev.password = password;
        userPrev.enable = false;
        return userPrev;
    }

    public void confirmSuccess() {
        this.enable = true;
    }
}
