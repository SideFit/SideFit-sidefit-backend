package com.project.sidefit.api.controller;

import com.project.sidefit.api.dto.response.Response;
import com.project.sidefit.domain.entity.User;
import com.project.sidefit.domain.repository.project.ProjectRepository;
import com.project.sidefit.domain.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.project.sidefit.api.dto.ProjectDto.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProjectApiController {

    private final ProjectService projectService;
    private final ProjectRepository projectRepository;

    @GetMapping("/project/{projectId}")
    public Response getProject(@PathVariable String projectId) {
        return Response.success(projectService.findProjectDto(Long.valueOf(projectId)));
    }

    @PostMapping("/project")
    public Response createProject(@AuthenticationPrincipal User user, @RequestParam(required = false) String imageId, @RequestBody ProjectRequestDto projectRequestDto) {
        if (imageId.isEmpty() && projectRequestDto.getImageUrl().isEmpty()) {
            return Response.failure(-1000, "이미지를 선택해주세요.");
        }
        Long projectId = projectService.makeProject(user.getId(), Long.valueOf(imageId), projectRequestDto);
        return Response.success();
    }

    // TODO: 어떤 필드를 업데이트 할 수 있는지?
    @PatchMapping("/project")
    public Response updateProject(@AuthenticationPrincipal User user, @RequestParam String projectId, @RequestParam(required = false) String imageId,
                                  @RequestBody ProjectRequestDto projectRequestDto) {
        ProjectResponseDto project = projectService.findProjectDto(Long.valueOf(projectId));
        if (project.getUserId().equals(user.getId())) {
            return Response.failure(-1000, "프로젝트 수정 권한이 없습니다.");
        }
        if (imageId.isEmpty() && projectRequestDto.getImageUrl().isEmpty()) {
            return Response.failure(-1000, "이미지를 선택해주세요.");
        }
        projectService.updateProject(project.getId(), Long.valueOf(imageId), projectRequestDto);
        return Response.success(projectService.findProjectDto(project.getId()));
    }

    @PatchMapping("/project/end")
    public Response endProject(@AuthenticationPrincipal User user, @RequestParam String projectId) {
        ProjectResponseDto project = projectService.findProjectDto(Long.valueOf(projectId));
        if (!project.getUserId().equals(user.getId())) {
            return Response.failure(-1000, "프로젝트 종료 권한이 없습니다.");
        }
        projectService.endProject(project.getId());
        return Response.success();
    }

    @DeleteMapping("/project/delete")
    public Response deleteProject(@AuthenticationPrincipal User user, @RequestParam String projectId) {
        ProjectResponseDto project = projectService.findProjectDto(Long.valueOf(projectId));
        if (!project.getUserId().equals(user.getId())) {
            return Response.failure(-1000, "프로젝트 삭제 권한이 없습니다.");
        }
        projectService.deleteProject(project.getId());
        return Response.success();
    }

    @GetMapping("/project/{projectId}/member/list")
    public Response getProjectMembers(@PathVariable String projectId) {
        return Response.success(projectService.findProjectUserDtoListWithProjectId(Long.valueOf(projectId)));
    }

    @GetMapping("/project/pre-member/list")
    public Response getProjectPreMembers(@AuthenticationPrincipal User user) {
        List<MemberResponseDto> preMembers = projectService.findPreMemberDtoListWithUserId(user.getId());
        preMembers.removeIf(preMember -> preMember.getId().equals(user.getId())); // 현재 로그인한 회원 제외

        return Response.success(preMembers);
    }

    @GetMapping("/project/recommend/list")
    public Response getRecommendProjects(@AuthenticationPrincipal User user) {
        return Response.success(projectService.findRecommendProjectDtoListWithUserId(user.getId()));
    }
}
