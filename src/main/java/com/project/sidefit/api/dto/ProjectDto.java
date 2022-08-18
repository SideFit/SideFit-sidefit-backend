package com.project.sidefit.api.dto;

import com.project.sidefit.domain.entity.Project;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class ProjectDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectRequestDto {

        private String imageId;

        @NotBlank
        private String title;

        @NotNull
        private Integer type; // 0: 출시 목적, 1: 포트폴리오, 2: 토이 프로젝트

        @NotNull
        private String field;

        @NotBlank
        private String introduction;

        @NotNull
        private String period;

        @NotNull
        private String stack;

        private String meetingPlan;

        @NotNull
        private String hashtag;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectResponseDto {

        private Long id;
        private Long userId;
        private Long imageId;
        private String title;
        private Integer type; // 0: 출시 목적, 1: 포트폴리오, 2: 토이 프로젝트
        private String introduction;
        private String period;
        private String meetingPlan;
        private String hashtag;
        private LocalDateTime createdDate;
        private LocalDateTime lastModifiedDate;

        public ProjectResponseDto(Project project) {
            id = project.getId();
            userId = project.getUser().getId();
            imageId = project.getImage().getId();
            title = project.getTitle();
            type = project.getType();
            introduction = project.getIntroduction();
            period = project.getPeriod();
            meetingPlan = project.getMeetingPlan();
            hashtag = project.getHashtag();
            createdDate = project.getCreatedDate();
            lastModifiedDate = project.getLastModifiedDate();
            projectUsers = project.getProjectUsers().stream()
                    .map(ProjectUserResponseDto::new)
                    .collect(Collectors.toList());
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectUserResponseDto {

        private Long id;
        private Long userId;
        private Long projectId;

        public ProjectUserResponseDto(ProjectUser projectUser) {
            id = projectUser.getId();
            userId = projectUser.getUser().getId();
            projectId = projectUser.getProject().getId();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberResponseDto {

        private Long id;
        private Long imageId;
        private String nickname;
        private String job;

        public MemberResponseDto(User user) {
            id = user.getId();
            imageId = user.getImage().getId();
            nickname = user.getNickname();
            job = user.getJob();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectRecommendDto {

        private Long id;
        private Long userId;
        private Long imageId;
        private String title;
        private Integer type; // 0: 출시 목적, 1: 포트폴리오, 2: 토이 프로젝트
        private String hashtag;
        private LocalDateTime createdDate;
        private LocalDateTime lastModifiedDate;

        public ProjectRecommendDto(Project project) {
            id = project.getId();
            userId = project.getUser().getId();
            imageId = project.getImage().getId();
            title = project.getTitle();
            type = project.getType();
            hashtag = project.getHashtag();
            createdDate = project.getCreatedDate();
            lastModifiedDate = project.getLastModifiedDate();
        }
    }
}
