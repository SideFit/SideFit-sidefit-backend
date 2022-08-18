package com.project.sidefit.api.dto;

import com.project.sidefit.domain.entity.Apply;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class ApplyDto {

    // 프로젝트 지원
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplyRequestDto {

        @NotBlank
        private String jobGroup;

        private String comment;
    }

    // 유저 초대
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InviteRequestDto {

        @NotBlank
        private String jobGroup;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplyResponseDto {

        private Long id;
        private Long userId;
        private Long projectId;
        private String jobGroup;
        private String comment;
        private Integer status; // 0: 자신이 신청, 1: 팀장이 초대, 2: 수락, 3: 거절
        private LocalDateTime createdDate;
        private LocalDateTime lastModifiedDate;

        public ApplyResponseDto(Apply apply) {
            id = apply.getId();
            userId = apply.getUser().getId();
            projectId = apply.getProject().getId();
            jobGroup = apply.getJobGroup();
            comment = apply.getComment();
            status = apply.getStatus();
            createdDate = apply.getCreatedDate();
            lastModifiedDate = apply.getLastModifiedDate();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplyResultDto {

        private Long id;
        private Long userId;
        private Long projectId;
        private Integer status; // 0: 자신이 신청, 1: 팀장이 초대, 2: 수락, 3: 거절
        private LocalDateTime createdDate;
        private LocalDateTime lastModifiedDate;

        public ApplyResultDto(Apply apply) {
            id = apply.getId();
            userId = apply.getUser().getId();
            projectId = apply.getProject().getId();
            status = apply.getStatus();
            createdDate = apply.getCreatedDate();
            lastModifiedDate = apply.getLastModifiedDate();
        }
    }
}
