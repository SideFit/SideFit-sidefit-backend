package com.project.sidefit.api.controller;

import com.project.sidefit.config.security.JwtProvider;
import com.project.sidefit.domain.repository.notification.NotificationRepository;
import com.project.sidefit.domain.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

import static com.project.sidefit.api.dto.NotificationDto.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NotificationApiController {

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final JwtProvider jwtProvider;

    // TODO: 편의상 userId 를 받게 하였지만, 추후 jwt token 을 받을 예정
    @GetMapping(value = "/sse/connect/{userId}", produces = "text/event-stream")
    @ResponseStatus(HttpStatus.OK)
    public SseEmitter connect(@PathVariable String userId, @RequestHeader(value = "Last-Event_ID", defaultValue = "", required = false) String lastEventId) {
//        Authentication authentication = jwtProvider.getAuthentication(token);
//        UserDetails principal = (UserDetails) authentication.getPrincipal();
//        String userId = principal.getUsername();
        return notificationService.connect(Long.valueOf(userId), lastEventId);
    }

    @PostMapping("/notification/send")
    public NotificationResponseDto sendNotification(@RequestBody NotificationRequestDto notificationRequestDto) {
        Long notificationId = notificationService.sendNotification(notificationRequestDto);
        return notificationService.findNotificationDto(notificationId);
    }

    @GetMapping("/notification/list")
    public List<NotificationQueryDto> getNotifications(@RequestParam String receiverId) {
        return notificationRepository.findNotificationsWithReceiverId(Long.valueOf(receiverId));
    }
}
