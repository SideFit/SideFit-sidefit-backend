package com.project.sidefit.api.controller;

import com.project.sidefit.api.dto.response.Response;
import com.project.sidefit.domain.entity.User;
import com.project.sidefit.domain.repository.ApplyRepository;
import com.project.sidefit.domain.repository.UserJpaRepo;
import com.project.sidefit.domain.repository.project.ProjectRepository;
import com.project.sidefit.domain.service.ApplyService;
import com.project.sidefit.domain.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.project.sidefit.api.dto.ApplyDto.*;
import static com.project.sidefit.api.dto.ProjectDto.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApplyApiController {

    private final ApplyService applyService;
    private final ProjectService projectService;
    private final ApplyRepository applyRepository;
    private final UserJpaRepo userRepository;
    private final ProjectRepository projectRepository;

    @PostMapping("/project/apply")
    public Response apply(@AuthenticationPrincipal User user, @RequestParam String projectId, @RequestBody ApplyRequestDto ApplyRequestDto) {
        Long applyId = applyService.applyToTeam(user.getId(), Long.valueOf(projectId), ApplyRequestDto);
        return Response.success(applyService.findApplyDto(applyId));
    }

    @PostMapping("/project/invite")
    public Response invite(@AuthenticationPrincipal User user, @RequestParam String receiverId, @RequestParam String projectId, @RequestBody InviteRequestDto inviteRequestDto) {
        ProjectResponseDto project = projectService.findProjectDto(Long.valueOf(projectId));
        if (!project.getUserId().equals(user.getId())) {
            return Response.failure(-1000, "프로젝트 관리 권한이 없습니다.");
        }
        Long applyId = applyService.inviteToUser(Long.valueOf(receiverId), Long.valueOf(projectId), inviteRequestDto);
        return Response.success(applyService.findApplyDto(applyId));
    }

    // 프로젝트 팀장이 유저 지원 처리
    @GetMapping("/project/apply-response/{applyId}")
    public Response getApplyResponse(@AuthenticationPrincipal User user, @PathVariable String applyId, @RequestParam Boolean flag) {
        ApplyResponseDto apply = applyService.findApplyDto(Long.valueOf(applyId));
        ProjectResponseDto project = projectService.findProjectDto(apply.getProjectId());
        if (!project.getUserId().equals(user.getId())) {
            return Response.failure(-1000, "프로젝트 관리 권한이 없습니다.");
        }
        if (userRepository.findById(apply.getUserId()).isEmpty()) {
            return Response.failure(-1000, "유저를 찾을 수 없습니다.");
        }
        applyService.applyResponse(Long.valueOf(applyId), flag);
        return Response.success(applyService.findApplyResultDto(Long.valueOf(applyId)));
    }

    // 유저가 프로젝트 참여 제안 처리
    @GetMapping("/project/invite-response/{applyId}")
    public Response getInviteResponse(@AuthenticationPrincipal User user, @PathVariable String applyId, @RequestParam Boolean flag) {
        ApplyResponseDto apply = applyService.findApplyDto(Long.valueOf(applyId));
        if (projectRepository.findById(apply.getProjectId()).isEmpty()) {
            return Response.failure(-1000, "프로젝트가 존재하지 않습니다.");
        }
        applyService.inviteResponse(Long.valueOf(applyId), flag);
        return Response.success(applyService.findApplyResultDto(Long.valueOf(applyId)));
    }
}