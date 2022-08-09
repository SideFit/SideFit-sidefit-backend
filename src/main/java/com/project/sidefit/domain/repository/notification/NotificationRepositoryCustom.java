package com.project.sidefit.domain.repository.notification;

import java.util.List;

import static com.project.sidefit.api.dto.NotificationDto.*;

public interface NotificationRepositoryCustom {

    List<NotificationQueryDto> findNotificationsWithReceiverId(Long receiverId);
}
