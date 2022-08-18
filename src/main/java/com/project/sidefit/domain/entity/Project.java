package com.project.sidefit.domain.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Project extends BaseTime {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private Image image;

    @OneToMany(mappedBy = "project", orphanRemoval = true)
    private List<ProjectUser> projectUsers = new ArrayList<>();

    @Column(length = 20, unique = true)
    private String title;

    @Column(columnDefinition = "tinyint")
    private int type; // 0: 출시 목적, 1: 포트폴리오, 2: 토이 프로젝트

    @Column(length = 10)
    private String field;

    @Column(length = 120)
    private String introduction;

    @Column(length = 20)
    private String period;

    @Column(length = 20)
    private String stack; // # 태그 입력

    @Column(length = 10)
    private String meetingPlan;

    @Column(length = 300)
    private String hashtag; // 해시 태그 (# 태그 입력)

    private boolean status; // 현재 진행 여부 (true: 진행중, false: 종료)

    @Builder
    private Project(User user, Image image, String title, int type, String field, String introduction, String period, String stack, String meetingPlan, String hashtag, boolean status) {
        this.user = user;
        this.image = image;
        this.title = title;
        this.type = type;
        this.field = field;
        this.introduction = introduction;
        this.period = period;
        this.stack = stack;
        this.meetingPlan = meetingPlan;
        this.hashtag = hashtag;
        this.status = status;
    }

    public void update(Image image, String title, int type, String field, String introduction, String period, String stack, String meetingPlan, String hashtag) {
        this.image = image;
        this.title = title;
        this.type = type;
        this.field = field;
        this.introduction = introduction;
        this.period = period;
        this.stack = stack;
        this.meetingPlan = meetingPlan;
        this.hashtag = hashtag;
    }

    // TODO: User column 에 맞게 수정, 각 column 의 입력 방식에 따라 수정
    public int recommendScoreByUser(User user) {
        int score = 0;
//        if (user.getFavorite().equals(field)) {
//            score += 5;
//        }
//        if (user.getStack().equals(stack)) {
//            score += 3;
//        }
//        if (user.getStatus().equals(type)) {
//            score += 2;
//        }
        return score;
    }

    public void end() {
        if (status) {
            status = false;
        } else {
            throw new IllegalStateException("이미 종료된 프로젝트입니다.");
        }
    }
}
