package com.project.sidefit.api.controller;

import com.project.sidefit.api.dto.response.Response;
import com.project.sidefit.domain.repository.ApplyRepository;
import com.project.sidefit.domain.repository.UserJpaRepo;
import com.project.sidefit.domain.service.ApplyService;
import com.project.sidefit.domain.service.ProjectService;
import com.project.sidefit.domain.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.project.sidefit.api.dto.ApplyDto.*;
import static com.project.sidefit.api.dto.NotificationDto.*;
import static com.project.sidefit.api.dto.ProjectDto.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApplyApiController {

    private final ApplyService applyService;
    private final ProjectService projectService;
    private final NotificationService notificationService;
    private final UserJpaRepo userRepository;
    private final ApplyRepository applyRepository;

    // TODO: 현재 로그인 중인 유저: userId -> 수정 예정
    @PostMapping("/project/apply")
    public Response apply(@RequestParam String userId, @RequestParam String projectId, @RequestBody ApplyRequestDto ApplyRequestDto) {
        ProjectResponseDto projectResponseDto;
        try {
            projectResponseDto = projectService.findProjectDto(Long.valueOf(projectId));
        } catch (Exception e) {
            return Response.failure(404, "팀 정보를 찾을 수 없습니다.");
        }
        Long applyId = applyService.applyToTeam(Long.valueOf(userId), Long.valueOf(projectId), ApplyRequestDto);
        List<NotificationResponseDto> notificationDtoList = notificationService.findNotificationDtoListWithSenderAndReceiverId(Long.valueOf(userId), projectResponseDto.getUserId());

        Map<String, Object> data = new HashMap<>();
        data.put("apply", applyService.findApplyDto(applyId));
        data.put("notifications", notificationDtoList);

        return Response.success(data);
    }

    // TODO: 현재 로그인 중인 유저 id: userId -> 수정 예정
    @PostMapping("/project/invite")
    public Response invite(@RequestParam String receiverId, @RequestParam String projectId, @RequestBody InviteRequestDto inviteRequestDto) {
        if (userRepository.findById(Long.valueOf(receiverId)).isEmpty()) {
            return Response.failure(404, "유저를 찾을 수 없습니다.");
        }
        ProjectResponseDto project = projectService.findProjectDto(Long.valueOf(projectId));
//        if (!project.getUserId().equals(Long.valueOf(principal.getUserId()))) {
//            return Response.failure(400, "권한이 없습니다.");
//        }
        Long applyId = applyService.inviteToUser(Long.valueOf(receiverId), Long.valueOf(projectId), inviteRequestDto);
        List<NotificationResponseDto> notificationDtoList = notificationService.findNotificationDtoListWithSenderAndReceiverId(project.getUserId(), Long.valueOf(receiverId));

        Map<String, Object> data = new HashMap<>();
        data.put("apply", applyService.findApplyDto(applyId));
        data.put("notifications", notificationDtoList);

        return Response.success(data);
    }

    @GetMapping("/project/apply/{applyId}")
    public Response getApplyRequest(@PathVariable String applyId, @RequestParam Boolean flag) {
        if (applyRepository.findById(Long.valueOf(applyId)).isEmpty()) {
            return Response.failure(404, "지원 내역을 찾을 수 없습니다.");
        }
        applyService.applyRequest(Long.valueOf(applyId), flag);
        return Response.success();
    }

    @GetMapping("/project/invite/{applyId}")
    public Response getInviteRequest(@PathVariable String applyId, @RequestParam Boolean flag) {
        if (applyRepository.findById(Long.valueOf(applyId)).isEmpty()) {
            return Response.failure(404, "지원 내역을 찾을 수 없습니다.");
        }
        applyService.inviteRequest(Long.valueOf(applyId), flag);
        return Response.success();
    }
}
