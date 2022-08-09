package com.project.sidefit.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import static com.project.sidefit.api.dto.NotificationDto.*;

@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final NotificationService notificationService;

    @TransactionalEventListener
    @Async
    public void handleNotification(NotificationRequestDto notificationRequestDto) {
        notificationService.sendNotification(notificationRequestDto);
    }
}
