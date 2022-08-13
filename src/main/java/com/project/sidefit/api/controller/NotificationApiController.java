package com.project.sidefit.api.controller;

import com.project.sidefit.api.dto.response.Response;
import com.project.sidefit.config.security.JwtProvider;
import com.project.sidefit.domain.entity.User;
import com.project.sidefit.domain.repository.notification.NotificationRepository;
import com.project.sidefit.domain.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import static com.project.sidefit.api.dto.NotificationDto.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NotificationApiController {

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final JwtProvider jwtProvider;

    @GetMapping(value = "/sse/connect", produces = "text/event-stream")
    @ResponseStatus(HttpStatus.OK)
    public Response connect(@RequestHeader(value = "X-AUTH-TOKEN", required = false) String token,
                            @RequestHeader(value = "Last-Event-ID", defaultValue = "", required = false) String lastEventId) {
        if (token.isEmpty()) {
            return Response.failure(400, "토큰이 존재하지 않습니다.");
        }
        Authentication authentication = jwtProvider.getAuthentication(token);
        User principal = (User) authentication.getPrincipal();
        notificationService.connect(principal.getId(), lastEventId);

        return Response.success();
    }

    @PostMapping("/notification/send")
    public Response sendNotification(@RequestHeader(value = "X-AUTH-TOKEN", required = false) String token, @RequestParam String receiverId,
                                     @RequestBody NotificationRequestDto notificationRequestDto) {
        if (token.isEmpty()) {
            return Response.failure(400, "토큰이 존재하지 않습니다.");
        }
        Authentication authentication = jwtProvider.getAuthentication(token);
        User principal = (User) authentication.getPrincipal();
        notificationService.sendNotification(notificationRequestDto, Long.valueOf(principal.getUsername()), Long.valueOf(receiverId));

        return Response.success(notificationService.findNotificationDtoListWithSenderAndReceiverId(principal.getId(), Long.valueOf(receiverId)));
    }

    @GetMapping("/notification/list")
    public Response getNotifications(@RequestHeader(value = "X-AUTH-TOKEN", required = false) String token) {
        if (token.isEmpty()) {
            return Response.failure(400, "토큰이 존재하지 않습니다.");
        }
        Authentication authentication = jwtProvider.getAuthentication(token);
        User principal = (User) authentication.getPrincipal();

        return Response.success(notificationRepository.findNotificationsWithReceiverId(principal.getId()));
    }
}
