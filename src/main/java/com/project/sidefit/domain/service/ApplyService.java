package com.project.sidefit.domain.service;

import com.project.sidefit.domain.entity.Apply;
import com.project.sidefit.domain.entity.Project;
import com.project.sidefit.domain.entity.ProjectUser;
import com.project.sidefit.domain.entity.user.User;
import com.project.sidefit.domain.enums.NotificationType;
import com.project.sidefit.domain.repository.ApplyRepository;
import com.project.sidefit.domain.repository.user.UserRepository;
import com.project.sidefit.domain.repository.project.ProjectRepository;
import com.project.sidefit.domain.repository.project.ProjectUserRepository;
import com.project.sidefit.domain.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.project.sidefit.api.dto.ApplyDto.*;
import static com.project.sidefit.api.dto.NotificationDto.*;
import static com.project.sidefit.api.dto.ProjectDto.*;

@Service
@Transactional
@RequiredArgsConstructor
public class ApplyService {

    private final ApplyRepository applyRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectUserRepository projectUserRepository;
    private final NotificationService notificationService;

    // 프로젝트 지원
    public Long applyToTeam(Long userId, Long projectId, ApplyRequestDto applyRequestDto) {
        User user = userRepository.getReferenceById(userId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalStateException("This project is null: " + projectId));
        if (applyRepository.findByUser(user).stream().anyMatch(apply -> apply.getProject().equals(project) && apply.getStatus() == 0)) {
            throw new IllegalStateException("이미 지원하신 프로젝트입니다. 잠시만 기다려주세요.");
        }
        if (project.getUser().getId().equals(userId)) {
            throw new IllegalStateException("자신의 프로젝트에는 지원하실 수 없습니다.");
        }
        Apply apply = Apply.builder()
                .user(user)
                .project(project)
                .jobGroup(applyRequestDto.getJobGroup())
                .comment(applyRequestDto.getComment())
                .status(0)
                .build();

        // 프로젝트 팀장에게 알림 전송
        NotificationRequestDto notificationRequestDto = new NotificationRequestDto(
                user.getNickname() + "님이 " + project.getTitle() + " " + applyRequestDto.getJobGroup() + "(으)로 지원했어요!",
                NotificationType.PERSONAL
        );
        notificationService.sendNotification(notificationRequestDto, user.getId(), project.getUser().getId());

        return applyRepository.save(apply).getId();
    }

    // 유저 초대
    public Long inviteToUser(Long userId, Long projectId, InviteRequestDto inviteRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("This user is null: " + userId));
        Project project = projectRepository.getReferenceById(projectId);
        if (applyRepository.findByProject(project).stream().anyMatch(apply -> apply.getUser().equals(user) && apply.getStatus() == 1)) {
            throw new IllegalStateException("이미 참여를 요청하신 유저입니다. 잠시만 기다려주세요.");
        }
        Apply apply = Apply.builder()
                .user(user)
                .project(project)
                .jobGroup(inviteRequestDto.getJobGroup())
                .comment(null)
                .status(1)
                .build();

        // 유저에게 알림 전송
        NotificationRequestDto notificationRequestDto = new NotificationRequestDto(
                project.getUser().getNickname() + "님이 " + project.getTitle() + " " + inviteRequestDto.getJobGroup() + "(으)로 초대했어요!",
                NotificationType.PERSONAL
        );
        notificationService.sendNotification(notificationRequestDto, project.getUser().getId(), user.getId());

        return applyRepository.save(apply).getId();
    }

    // 프로젝트 팀장이 유저 지원 처리
    public void applyResponse(Long applyId, boolean flag) {
        Apply apply = findApply(applyId);
        Long userId = apply.getUser().getId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("This user is null: " + userId));
        Project project = apply.getProject();
        NotificationRequestDto notificationRequestDto;

        if (flag) {
            apply.accept(project, user, 0);
            ProjectUser projectUser = ProjectUser.createProjectUser(user, project);
            projectUserRepository.save(projectUser);
            notificationRequestDto = new NotificationRequestDto(
                    project.getUser().getNickname() + "님이 " + user.getNickname() + "님의 지원 요청에 수락했어요!",
                    NotificationType.PERSONAL);
        } else {
            apply.reject(apply.getProject(), user, 0);
            notificationRequestDto = new NotificationRequestDto(
                    project.getUser().getNickname() + "님이 " + user.getNickname() + "님의 지원 요청에 거절했어요!",
                    NotificationType.PERSONAL);
        }
        notificationService.sendNotification(notificationRequestDto, project.getUser().getId(), userId);
    }

    // 유저가 프로젝트 참여 제안 처리
    public void inviteResponse(Long applyId, boolean flag) {
        Apply apply = findApply(applyId);
        Long projectId = apply.getProject().getId();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalStateException("This project is null: " + projectId));
        User user = apply.getUser();
        NotificationRequestDto notificationRequestDto;

        if (flag) {
            apply.accept(project, user, 1);
            ProjectUser projectUser = ProjectUser.createProjectUser(user, project);
            projectUserRepository.save(projectUser);
            notificationRequestDto = new NotificationRequestDto(
                    user.getNickname() + "님이 " + project.getUser().getNickname() + "님의 초대 요청에 수락했어요!",
                    NotificationType.PERSONAL);
        } else {
            apply.reject(project, apply.getUser(), 1);
            notificationRequestDto = new NotificationRequestDto(
                    user.getNickname() + "님이 " + project.getUser().getNickname() + "님의 초대 요청에 거절했어요!",
                    NotificationType.PERSONAL);
        }
        notificationService.sendNotification(notificationRequestDto, user.getId(), project.getUser().getId());
    }

    @Transactional(readOnly = true)
    public ApplyResponseDto findApplyDto(Long applyId) {
        return new ApplyResponseDto(findApply(applyId));
    }

    @Transactional(readOnly = true)
    public ApplyResultDto findApplyResultDto(Long applyId) {
        return new ApplyResultDto(findApply(applyId));
    }

    @Transactional(readOnly = true)
    public List<ApplyResponseDto> findApplyDtoList() {
        return applyRepository.findAll().stream()
                .map(ApplyResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ApplyProjectDto> findApplyProjectDtoWithUser(Long userId) {
        User user = userRepository.getReferenceById(userId);
        List<Project> projects = applyRepository.findByUser(user).stream()
                .map(Apply::getProject)
                .collect(Collectors.toList());

        return projects.stream()
                .map(ApplyProjectDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    private Apply findApply(Long applyId) {
        return applyRepository.findById(applyId)
                .orElseThrow(() -> new IllegalStateException("This apply is null: " + applyId));
    }
}
