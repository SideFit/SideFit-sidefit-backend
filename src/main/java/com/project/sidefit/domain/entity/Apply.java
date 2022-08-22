package com.project.sidefit.domain.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Apply extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(length = 10)
    private String jobGroup;

    @Column(length = 100)
    private String comment;

    @Column(columnDefinition = "tinyint")
    private int status; // 0: 자신이 신청, 1: 팀장이 초대, 2: 수락, 3: 거절

    @Builder
    private Apply(User user, Project project, String jobGroup, String comment, int status) {
        this.user = user;
        this.project = project;
        this.jobGroup = jobGroup;
        this.comment = comment;
        this.status = status;
    }

    // 참여 or 초대 요청 수락
    public void accept(Project project, User user, int flag) {
        // 이미 참여한 상태인지 검증
        checkAlreadyParticipated(project, user);
        // 요청 검증
        if (status == 0 || status == 1) {
            if (status == flag) {
                status = 2;
                project.getRecruits().forEach(recruit -> {
                    if (recruit.getJobGroup().equals(jobGroup)) {
                        recruit.recruitComplete();
                    }
                });
            } else {
                throw new IllegalStateException("잘못된 요청입니다.");
            }
        } else {
            throw new IllegalStateException("지원 요청 상태가 아닙니다.");
        }
    }

    // 참여 or 초대 요청 거절
    public void reject(Project project, User user, int flag) {
        // 이미 참여한 상태인지 검증
        checkAlreadyParticipated(project, user);
        // 요청 검증
        if (status == 0 || status == 1) {
            if (status == flag) {
                status = 3;
            } else {
                throw new IllegalStateException("잘못된 요청입니다.");
            }
        } else {
            throw new IllegalStateException("지원 요청 상태가 아닙니다.");
        }
    }

    private void checkAlreadyParticipated(Project project, User user) {
        project.getProjectUsers().forEach(projectUser -> {
            if (projectUser.getUser().equals(user)) {
                throw new IllegalStateException("이미 참여한 프로젝트입니다.");
            }
        });
    }
}
