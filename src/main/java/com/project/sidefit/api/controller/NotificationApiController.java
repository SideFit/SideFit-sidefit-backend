package com.project.sidefit.api.controller;

import com.project.sidefit.api.dto.response.Response;
import com.project.sidefit.domain.entity.User;
import com.project.sidefit.domain.repository.UserJpaRepo;
import com.project.sidefit.domain.repository.notification.NotificationRepository;
import com.project.sidefit.domain.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.project.sidefit.api.dto.NotificationDto.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NotificationApiController {

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final UserJpaRepo userRepository;

    @GetMapping(value = "/sse/connect", produces = "text/event-stream")
    @ResponseStatus(HttpStatus.OK)
    public Response connect(@RequestHeader(value = "Last-Event-ID", defaultValue = "", required = false) String lastEventId, @AuthenticationPrincipal User user) {
        if (user == null) {
            return Response.failure(404, "인증된 사용자가 아닙니다.");
        }
        notificationService.connect(user.getId(), lastEventId);
        return Response.success();
    }

    @PostMapping("/notification/send")
    public Response sendNotification(@AuthenticationPrincipal User user, @RequestParam String receiverId, @RequestBody NotificationRequestDto notificationRequestDto) {
        if (user == null) {
            return Response.failure(404, "인증된 사용자가 아닙니다.");
        }
        if (userRepository.findById(Long.valueOf(receiverId)).isEmpty()) {
            return Response.failure(404, "유저가 존재하지 않습니다.");
        }
        notificationService.sendNotification(notificationRequestDto, user.getId(), Long.valueOf(receiverId));
        return Response.success(notificationService.findNotificationDtoListWithSenderAndReceiverId(Long.valueOf(user.getUsername()), Long.valueOf(receiverId)));
    }

    @GetMapping("/notification/list")
    public Response getNotifications(@AuthenticationPrincipal User user) {
        if (user == null) {
            return Response.failure(404, "인증된 사용자가 아닙니다.");
        }
        return Response.success(notificationRepository.findNotificationsWithReceiverId(user.getId()));
    }
}