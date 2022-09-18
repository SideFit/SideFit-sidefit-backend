package com.project.sidefit.domain.entity.user;

import com.project.sidefit.domain.entity.*;
import com.project.sidefit.domain.entity.time.BaseTime;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private Image image;

    // unique, not null
    @Column(nullable = false, unique = true)
    private String email;

    // not null
    private String password;

    // not null
    // TODO Role enum 단일값? or RoleType 엔티티 List?
//    @ElementCollection(fetch = FetchType.EAGER)
//    @CollectionTable(
//            name = "roles",
//            joinColumns = @JoinColumn(name = "user_id"))
//    private List<String> roles = new ArrayList<>();


    @Enumerated(EnumType.STRING)
    private Provider provider;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String providerId;



    // unique, not null
    @Column(nullable = false, unique = true)
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
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id")
    private List<Portfolio> portfolios = new ArrayList<>();


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

        user.role = Role.ROLE_USER;

        return user;
    }

    @Builder
    public User(String email, String nickname, Provider provider, String providerId, Role role) {
        this.email = email;
        this.nickname = nickname;
        this.provider = provider;
        this.providerId = providerId;
        this.role = role;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
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
                           List<CurrentStatus> currentStatuses, List<Tech> teches, List<Portfolio> portfolios, Mbti mbti, Image image) {

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

        this.portfolios.clear();
        this.portfolios.addAll(portfolios);

        this.mbti = mbti;
        this.image = image;
    }
}
