package com.project.sidefit.domain.entity.user;

import com.project.sidefit.domain.entity.time.BaseTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserPrev extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
