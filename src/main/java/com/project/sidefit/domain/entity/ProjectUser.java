package com.project.sidefit.domain.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectUser extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    private ProjectUser(User user) {
        this.user = user;
    }

    public static ProjectUser createProjectUser(User user, Project project) {
        ProjectUser projectUser = new ProjectUser(user);
        projectUser.setProject(project);

        return projectUser;
    }

    // 연관관계 편의 메소드
    private void setProject(Project project) {
        this.project = project;
        project.getProjectUsers().add(this);
    }
}
