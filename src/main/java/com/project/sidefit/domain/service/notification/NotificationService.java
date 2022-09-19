package com.project.sidefit.domain.service.notification;

import com.project.sidefit.domain.entity.Notification;
import com.project.sidefit.domain.entity.user.User;
import com.project.sidefit.domain.repository.user.UserRepository;
import com.project.sidefit.domain.repository.notification.EmitterRepository;
import com.project.sidefit.domain.repository.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.project.sidefit.api.dto.NotificationDto.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmitterRepository emitterRepository;
    private final UserRepository userRepository;

    private static final Long DEFAULT_TIMEOUT = 60 * 60 * 1000L;

    public SseEmitter connect(Long userId, String lastEventId) {
        String emitterId = userId + "_" + System.currentTimeMillis();
        SseEmitter emitter = emitterRepository.save(emitterId, new SseEmitter(DEFAULT_TIMEOUT));

        emitter.onCompletion(() -> {
            log.info("emitter completed, emitter id = {}", emitterId);
            emitterRepository.deleteById(emitterId);
        });
        emitter.onTimeout(() -> {
            log.info("emitter timeout, emitter id = {}", emitterId);
            emitterRepository.deleteById(emitterId);
        });
        sendMessage(emitter, emitterId, "EventStream Created. [userId = " + userId + "]");

        if (!lastEventId.isEmpty()) {
            Map<String, Object> events = emitterRepository.findEventCachesWithUserId(String.valueOf(userId));
            events.entrySet().stream()
                    .filter(entry -> entry.getKey().compareTo(lastEventId) > 0)
                    .forEach(entry -> sendMessage(emitter, entry.getKey(), entry.getValue()));
        }
        return emitter;
    }

    public Long sendNotification(NotificationRequestDto notificationRequestDto, Long senderId, Long receiverId) {
        User sender = userRepository.getReferenceById(senderId);
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalStateException("This receiver is null: " + receiverId));
        Notification notification = new Notification(sender, receiver, notificationRequestDto.getContent(), notificationRequestDto.getType());

        Map<String, SseEmitter> emitters = emitterRepository.findEmittersWithUserId(String.valueOf(receiverId));
        emitters.forEach(
                (emitterId, emitter) -> {
                    emitterRepository.saveEventCache(emitterId, notification);
                    sendMessage(emitter, String.valueOf(receiverId), new NotificationResponseDto(notification));
                }
        );

        return notificationRepository.save(notification).getId();
    }

    public void checkAll(List<Long> notificationIds) {
        for (Long notificationId : notificationIds) {
            Notification notification = findNotification(notificationId);
            notification.check();
        }
    }

    @Transactional(readOnly = true)
    public NotificationResponseDto findNotificationDto(Long notificationId) {
        return new NotificationResponseDto(findNotification(notificationId));
    }

    @Transactional(readOnly = true)
    public NotificationSimpleDto findCountAndImageUrlWithReceiverId(Long receiverId) {
        User receiver = userRepository.getReferenceById(receiverId);
        List<Notification> notifications = notificationRepository.findWithReceiverIdAndUnChecked(receiver.getId());

        return NotificationSimpleDto.builder()
                .receiverId(receiver.getId())
                .imageUrl(receiver.getImage().getImageUrl())
                .count(notifications.size())
                .build();
    }

    @Transactional(readOnly = true)
    private Notification findNotification(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalStateException("This notification is null: " + notificationId));
    }

    private void sendMessage(SseEmitter emitter, String id, Object data) {
        try {
            emitter.send(
                    SseEmitter.event()
                            .id(id)
                            .name("sse")
                            .data(data)
            );
        } catch (IOException e) {
            emitterRepository.deleteById(id);
            log.info("connection error!");
        }
    }
}
