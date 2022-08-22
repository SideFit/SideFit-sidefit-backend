package com.project.sidefit.domain.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Project extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private Image image; // 대표 이미지

    @OneToMany(mappedBy = "project", orphanRemoval = true)
    private List<ProjectUser> projectUsers = new ArrayList<>();

    @OneToMany(mappedBy = "project", orphanRemoval = true)
    private List<Recruit> recruits = new ArrayList<>();

    @Column(length = 20, unique = true)
    private String title; // 프로젝트 제목

    @Column(columnDefinition = "tinyint")
    private int type; // 프로젝트 유형 (0: 출시 목적, 1: 포트폴리오, 2: 토이 프로젝트)

    @Column(length = 10)
    private String field; // 프로젝트 분야

    @Column(length = 120)
    private String introduction; // 프로젝트 소개

    @Column(length = 20)
    private String period; // 예상 프로젝트 기간

    @Column(length = 300)
    private String stack; // 필요 스택 (# 태그 입력)

    @Column(length = 10)
    private String meetingPlan; // 모임 계획

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

    /**
     * 유저의 각 속성에 따른 점수 산정
     * 직무 -> 프로젝트 모집 (Recruit) : 10점
     * 기술 스택 -> 프로젝트 요구 스택 : 각 스택당 2점
     * 관심 분야 -> 프로젝트 분야 : 각 분야당 1점
     */
    public int recommendScoreByUser(User user) {
        // 직무
        List<String> jobGroups = recruits.stream()
                .filter(recruit -> recruit.getCurrentNumber() != 0)
                .map(Recruit::getJobGroup)
                .collect(Collectors.toList());
        String userJob = user.getJob();
        // 기술 스택
        String[] stacks = stack.split("#");
        List<String> userStacks = user.getTeches().stream().map(Tech::getStack).collect(Collectors.toList());
        // 관심 분야
        String[] tags = hashtag.split("#");
        List<String> userFavorites = user.getFavorites().stream().map(Favorite::getField).collect(Collectors.toList());

        int score = 0;

        if (jobGroups.contains(userJob)) {
            score += 10;
        }
        for (String stack : stacks) {
            if (userStacks.contains(stack)) {
                score += 2;
            }
        }
        for (String tag : tags) {
            if (userFavorites.contains(tag)) {
                score += 1;
            }
        }
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
