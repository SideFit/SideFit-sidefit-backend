package com.project.sidefit.api.controller;

import com.project.sidefit.api.dto.response.Response;
import com.project.sidefit.config.security.token.UserPrincipal;
import com.project.sidefit.domain.repository.user.UserRepository;
import com.project.sidefit.domain.repository.notification.NotificationRepository;
import com.project.sidefit.domain.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

import static com.project.sidefit.api.dto.NotificationDto.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NotificationApiController {

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping(value = "/sse/connect", produces = "text/event-stream")
    @ResponseStatus(HttpStatus.OK)
    public Response connect(@RequestHeader(value = "Last-Event-ID", defaultValue = "", required = false) String lastEventId, @AuthenticationPrincipal UserPrincipal user) {
        notificationService.connect(user.getId(), lastEventId);
        return Response.success();
    }

    @PostMapping("/notification/send")
    public Response sendNotification(@AuthenticationPrincipal UserPrincipal user, @RequestParam String receiverId, @Valid @RequestBody NotificationRequestDto notificationRequestDto) {
        notificationService.sendNotification(notificationRequestDto, user.getId(), Long.valueOf(receiverId));
        return Response.success();
    }

    @GetMapping("/notification/list")
    public Response getNotifications(@AuthenticationPrincipal UserPrincipal user) {
        List<NotificationQueryDto> notifications = notificationRepository.findNotificationsWithReceiverId(user.getId());
        List<Long> notificationIds = notifications.stream().map(NotificationQueryDto::getId).collect(Collectors.toList());
        notificationService.checkAll(notificationIds);

        return Response.success(notifications);
    }

    @GetMapping("/notification/simple")
    public Response getSimpleNotifications(@AuthenticationPrincipal UserPrincipal user) {
        return Response.success(notificationService.findCountAndImageUrlWithReceiverId(user.getId()));
    }
}