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
//@Builder
@Getter
@Table(name = "users")
//@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTime implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private Image image;

    // unique, not null
    private String email;

    // not null
    private String password;

    // not null
    // TODO Role enum 단일값? or RoleType 엔티티 List?
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
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id")
    private List<Tag> tags = new ArrayList<>();

    // 관심분야 >> 다중 선택
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id")
    private List<Favorite> favorites = new ArrayList<>();

    // 현재상태 >> 다중 선택
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id")
    private List<CurrentStatus> currentStatuses = new ArrayList<>();

    // 기술스택 >> 다중 선택
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id")
    private List<Tech> teches = new ArrayList<>();

    // Portfolio



    // mbti
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Mbti mbti;



    public static User createUser(String email, String password, String nickname, String job) {
        User user = new User();
        user.email = email;
        user.password = password;
        user.nickname = nickname;
        user.job = job;

        user.roles.add("ROLE_USER");

        return user;
    }

    public void updateMbti(Mbti mbti) {
        this.mbti = mbti;
    }

    public void updateImage(Image image) {
        this.image = image;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateUser(String job, String introduction, List<Tag> tags, List<Favorite> favorites,
                           List<CurrentStatus> currentStatuses, List<Tech> teches, Mbti mbti, Image image) {

        this.job = job;
        this.introduction = introduction;

        this.tags.clear();
        this.tags.addAll(tags);

        this.favorites.clear();
        this.favorites.addAll(favorites);

        this.currentStatuses.clear();
        this.currentStatuses.addAll(currentStatuses);

        this.teches.clear();
        this.teches.addAll(teches);

        this.mbti = mbti;
        this.image = image;
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
