package com.project.sidefit.domain.entity;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTime implements UserDetails {

    @Id
    @GeneratedValue
    @Column(name = "user_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private Image image;

    // unique, not null
    private String email;

    // not null
    private String password;

    // not null
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "roles",
            joinColumns = @JoinColumn(name = "user_id"))
    private List<String> roles = new ArrayList<>();

    // unique, not null
    private String nickname;

    // 직무
    // not null
    private String job;

    // 자기소개
    private String introduction;

    // 태그 >> #으로 여러개 작성 >> #구분자로 사용하면 될지?
    private String tag;

    // 관심분야 >> 다중 선택
    private String favorite;

    // 현재상태 >> 다중 선택
    private String status;

    // 기술스택 >> 다중 선택
    private String stack;

    // mbti?
    private String mbti;

    public static User createUser(String email, String password, String nickname, String job) {
        User user = new User();
        user.email = email;
        user.password = password;
        user.nickname = nickname;
        user.job = job;

        user.roles.add("ROLE_USER");

        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles
                .stream().map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return password;
    }

    // pk 반환
    @Override
    public String getUsername() {
        return String.valueOf(id);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
