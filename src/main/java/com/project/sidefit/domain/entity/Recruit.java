package com.project.sidefit.domain.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Recruit extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(length = 10)
    private String jobGroup;

    private int currentNumber; // 현재 인원

    private int recruitNumber; // 모집 인원

    private Recruit(String jobGroup, int currentNumber, int recruitNumber) {
        this.jobGroup = jobGroup;
        this.currentNumber = currentNumber;
        this.recruitNumber = recruitNumber;
    }

    public static Recruit create(Project project, String jobGroup, int recruitNumber) {
        Recruit recruit = new Recruit(jobGroup, 0, recruitNumber);
        recruit.setProject(project);

        return recruit;
    }

    public void recruitComplete() {
        int temp = currentNumber + 1;
        if (temp > recruitNumber) {
            throw new IllegalStateException("더 이상 모집할 수 없습니다.");
        }
        currentNumber = temp;
    }

    // 연관관계 편의 메소드
    private void setProject(Project project) {
        this.project = project;
        project.getRecruits().add(this);
    }
}
