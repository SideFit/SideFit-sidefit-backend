package com.project.sidefit.api.controller;

import com.project.sidefit.api.dto.response.Response;
import com.project.sidefit.domain.entity.user.User;
import com.project.sidefit.domain.enums.SearchCondition;
import com.project.sidefit.domain.repository.project.ProjectRepository;
import com.project.sidefit.domain.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static com.project.sidefit.api.dto.ProjectDto.*;
import static com.project.sidefit.domain.enums.SearchCondition.*;

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
    public Response createProject(@AuthenticationPrincipal User user, @RequestParam(required = false) String imageId, @Valid @RequestBody ProjectRequestDto projectRequestDto) {
        if (imageId == null && projectRequestDto.getImageUrl() == null) {
            return Response.failure(-1000, "이미지를 선택해주세요.");
        }
        return Response.success(projectService.makeProject(user.getId(), Long.valueOf(imageId), projectRequestDto));
    }

    @PatchMapping("/project")
    public Response updateProject(@AuthenticationPrincipal User user, @RequestParam String projectId, @RequestParam(required = false) String imageId,
                                  @Valid @RequestBody ProjectRequestDto projectRequestDto) {
        ProjectResponseDto project = projectService.findProjectDto(Long.valueOf(projectId));
        if (!project.getUserId().equals(user.getId())) {
            return Response.failure(-1000, "프로젝트 수정 권한이 없습니다.");
        }
        if (imageId == null && projectRequestDto.getImageUrl() == null) {
            return Response.failure(-1000, "이미지를 선택해주세요.");
        }
        projectService.updateProject(project.getId(), Long.valueOf(imageId), projectRequestDto);
        return Response.success();
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

    @DeleteMapping("/project")
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

    @GetMapping("/project/search")
    public Response searchProject(@RequestParam(required = false) String keyword, @RequestParam(defaultValue = "LATEST_ORDER") SearchCondition condition) {
        if (!StringUtils.hasText(keyword)) {
            return Response.failure(-1000, "키워드를 입력해주세요.");
        }
        if (condition == ACCURACY_ORDER) {
            return Response.success(projectRepository.searchProjectByAccuracyOrder(keyword));
        }
        return Response.success(projectRepository.searchProjectByLatestOrder(keyword));
    }

    @GetMapping("/project/search/recommend/list")
    public Response recommendKeyword() {
        return Response.success(projectService.findRecommendKeywordDtoList());
    }

    @GetMapping("/project/search/recommend/{keywordId}")
    public Response selectRecommendKeyword(@PathVariable String keywordId) {
        KeywordResponseDto keyword = projectService.findKeywordDto(Long.valueOf(keywordId));
        return Response.success(projectRepository.searchProjectByLatestOrder(keyword.getWord()));
    }
}
