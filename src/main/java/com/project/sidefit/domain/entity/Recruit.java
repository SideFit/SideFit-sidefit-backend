package com.project.sidefit.domain.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Recruit extends BaseTime {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(length = 10)
    private String jobGroup;

    private int recruitNumber;

    @Builder
    private Recruit(Project project, String jobGroup, int recruitNumber) {
        this.project = project;
        this.jobGroup = jobGroup;
        this.recruitNumber = recruitNumber;
    }
}
