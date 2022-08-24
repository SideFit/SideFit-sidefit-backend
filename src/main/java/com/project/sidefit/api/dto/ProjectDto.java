package com.project.sidefit.api.dto;

import com.project.sidefit.domain.entity.Project;
import com.project.sidefit.domain.entity.ProjectUser;
import com.project.sidefit.domain.entity.Recruit;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectRequestDto {

        @NotBlank(message = "제목을 입력해주세요.")
        private String title; // 프로젝트 제목

        @NotNull(message = "프로젝트 타입을 정해주세요.")
        private Integer type; // 0: 출시 목적, 1: 포트폴리오, 2: 토이 프로젝트

        @NotNull(message = "프로젝트 분야를 입력해주세요.")
        private String field; // 프로젝트 분야

        @NotBlank(message = "소개를 입력해주세요.")
        private String introduction; // 프로젝트 소개

        @NotNull(message = "예상 기간을 입력해주세요.")
        private String period; // 예상 프로젝트 기간

        @NotNull(message = "필요한 스택을 입력해주세요.")
        private String stack; // 필요 스택 (# 태그 입력)

        @NotNull(message = "모임 계획을 입력해주세요.")
        private String meetingPlan; // 모임 계획

        @NotNull(message = "해시 태그를 입력해주세요.")
        private String hashtag; // 해시 태그 (# 태그 입력)

        private String name; // 이미지 이름

        private String imageUrl; // 이미지 url

        private List<RecruitRequestDto> recruits; // 모집 직군 인원 리스트
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecruitRequestDto {

        @NotNull(message = "직군을 입력해주세요.")
        private String jobGroup;

        @NotNull(message = "인원을 정해주세요.")
        private Integer recruitNumber;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchRequestDto {

        @NotBlank(message = "검색하려는 키워드를 입력해주세요.")
        private String keyword;

        @NotNull
        private String query; // 최신순, 조회순..?
    }

    @Getter
    @NoArgsConstructor
    public static class ProjectQueryDto {

        // project
        private Long id;
        private String title;
        private Integer type; // 0: 출시 목적, 1: 포트폴리오, 2: 토이 프로젝트
        private String hashtag;
        private Boolean status;
        private LocalDateTime createdDate;
        private LocalDateTime lastModifiedDate;

        // image
        private Long imageId;
        private String imageUrl;

        // recruit
        private List<RecruitResponseDto> recruits;

        public void setRecruits(List<RecruitResponseDto> recruits) {
            this.recruits = recruits;
        }

        @QueryProjection
        public ProjectQueryDto(Long id, String title, Integer type, String hashtag, Boolean status, LocalDateTime createdDate, LocalDateTime lastModifiedDate, Long imageId, String imageUrl) {
            this.id = id;
            this.title = title;
            this.type = type;
            this.hashtag = hashtag;
            this.status = status;
            this.createdDate = createdDate;
            this.lastModifiedDate = lastModifiedDate;
            this.imageId = imageId;
            this.imageUrl = imageUrl;
        }
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
        private String stack;
        private String meetingPlan;
        private String hashtag;
        private Boolean status;
        private LocalDateTime createdDate;
        private LocalDateTime lastModifiedDate;
        private List<RecruitResponseDto> recruits;

        public ProjectResponseDto(Project project) {
            id = project.getId();
            userId = project.getUser().getId();
            imageId = project.getImage().getId();
            title = project.getTitle();
            type = project.getType();
            introduction = project.getIntroduction();
            period = project.getPeriod();
            stack = project.getStack();
            meetingPlan = project.getMeetingPlan();
            hashtag = project.getHashtag();
            status = project.isStatus();
            createdDate = project.getCreatedDate();
            lastModifiedDate = project.getLastModifiedDate();
            recruits = project.getRecruits().stream()
                    .map(RecruitResponseDto::new)
                    .collect(Collectors.toList());;
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecruitResponseDto {

        private Long id;
        private Long projectId;
        private String jobGroup;
        private Integer currentNumber;
        private Integer recruitNumber;

        public RecruitResponseDto(Recruit recruit) {
            id = recruit.getId();
            projectId = recruit.getProject().getId();
            jobGroup = recruit.getJobGroup();
            currentNumber = recruit.getCurrentNumber();
            recruitNumber = recruit.getRecruitNumber();
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
    public static class MemberResponseDto {

        // user
        private Long id;
        private String nickname;
        private String job;

        // image
        private Long imageId;
        private String imageUrl;

        @QueryProjection
        public MemberResponseDto(Long id, String nickname, String job, Long imageId, String imageUrl) {
            this.id = id;
            this.nickname = nickname;
            this.job = job;
            this.imageId = imageId;
            this.imageUrl = imageUrl;
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectRecommendDto {

        private Long id;
        private Long imageId;
        private String title;
        private Integer type; // 0: 출시 목적, 1: 포트폴리오, 2: 토이 프로젝트
        private String hashtag;
        private Boolean status;
        private LocalDateTime createdDate;
        private LocalDateTime lastModifiedDate;

        public ProjectRecommendDto(Project project) {
            id = project.getId();
            imageId = project.getImage().getId();
            title = project.getTitle();
            type = project.getType();
            hashtag = project.getHashtag();
            status = project.isStatus();
            createdDate = project.getCreatedDate();
            lastModifiedDate = project.getLastModifiedDate();
        }
    }
}
