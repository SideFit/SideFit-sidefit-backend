package com.project.sidefit.api.dto;

import com.project.sidefit.domain.entity.Notification;
import com.project.sidefit.domain.enums.NotificationType;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class NotificationDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationRequestDto {

        @NotBlank(message = "내용을 입력해주세요.")
        private String content;

        @NotNull(message = "알림 타입을 정해주세요.")
        private NotificationType type; // CHAT, PERSONAL
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationListenDto {

        @NotNull
        private String senderId;

        @NotNull
        private String receiverId;

        @NotBlank
        private String content;

        @NotNull
        private NotificationType type;
    }

    @Getter
    @NoArgsConstructor
    public static class NotificationResponseDto {

        private Long id;
        private Long senderId;
        private Long receiverId;
        private String content;
        private NotificationType type; // CHAT, PERSONAL
        private LocalDateTime createdDate;
        private LocalDateTime lastModifiedDate;

        public NotificationResponseDto(Notification notification) {
            id = notification.getId();
            senderId = notification.getSender().getId();
            receiverId = notification.getReceiver().getId();
            content = notification.getContent();
            type = notification.getType();
            createdDate = notification.getCreatedDate();
            lastModifiedDate = notification.getLastModifiedDate();
        }
    }

    @Getter
    @NoArgsConstructor
    public static class NotificationQueryDto {

        private Long id;
        private Long senderId;
        private Long receiverId;
        private String content;
        private NotificationType type;
        private LocalDateTime createdDate;
        private LocalDateTime lastModifiedDate;

        private Long imageId; // sender
        private String nickname; // sender

        @QueryProjection
        public NotificationQueryDto(Long id, Long senderId, Long receiverId, String content, NotificationType type, LocalDateTime createdDate, LocalDateTime lastModifiedDate, Long imageId, String nickname) {
            this.id = id;
            this.senderId = senderId;
            this.receiverId = receiverId;
            this.content = content;
            this.type = type;
            this.createdDate = createdDate;
            this.lastModifiedDate = lastModifiedDate;
            this.imageId = imageId;
            this.nickname = nickname;
        }
    }
}
