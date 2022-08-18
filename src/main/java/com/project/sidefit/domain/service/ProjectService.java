package com.project.sidefit.domain.service;

import com.project.sidefit.domain.entity.*;
import com.project.sidefit.domain.repository.ImageRepository;
import com.project.sidefit.domain.repository.UserJpaRepo;
import com.project.sidefit.domain.repository.project.ProjectRepository;
import com.project.sidefit.domain.repository.project.ProjectUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.project.sidefit.api.dto.ImageDto.*;
import static com.project.sidefit.api.dto.ProjectDto.*;

@Service
@Transactional
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectUserRepository projectUserRepository;
    private final UserJpaRepo userRepository;
    private final ImageRepository imageRepository;

    public Long makeProject(Long userId, Long imageId, ProjectRequestDto projectRequestDto, ImageRequestDto imageRequestDto) {
        User user = userRepository.getReferenceById(userId);
        Image image = selectOrSaveImage(imageId, imageRequestDto);
        Project project = createProject(projectRequestDto, user, image);
        ProjectUser projectUser = ProjectUser.createProjectUser(user, project);

        projectUserRepository.save(projectUser);
        return projectRepository.save(project).getId();
    }

    // TODO: 어떤 필드를 수정하는지?
    public void updateProject(Long projectId, Long imageId, ProjectRequestDto projectRequestDto, ImageRequestDto imageRequestDto) {
        Project project = findProject(projectId);
        if (!project.isStatus()) {
            throw new IllegalStateException("이미 종료된 프로젝트입니다.");
        }
        Image image = selectOrSaveImage(imageId, imageRequestDto);
        project.update(image,
                projectRequestDto.getTitle(),
                projectRequestDto.getType(),
                projectRequestDto.getField(),
                projectRequestDto.getIntroduction(),
                projectRequestDto.getPeriod(),
                projectRequestDto.getStack(),
                projectRequestDto.getMeetingPlan(),
                projectRequestDto.getHashtag()
        );
    }

    public void deleteProject(Long projectId) {

    }

    @Transactional(readOnly = true)
    public ProjectResponseDto findProjectDto(Long teamId) {
        return new ProjectResponseDto(findProject(teamId));
    }

    @Transactional(readOnly = true)
    public List<ProjectResponseDto> findProjectDtoList() {
        return projectRepository.findAll().stream()
                .map(ProjectResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    private Project findProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalStateException("This team is null: " + projectId));
    }

    private Project createProject(ProjectRequestDto projectRequestDto, User user, Image image) {
        return Project.builder()
                .user(user)
                .image(image)
                .title(projectRequestDto.getTitle())
                .type(projectRequestDto.getType())
                .field(projectRequestDto.getField())
                .introduction(projectRequestDto.getIntroduction())
                .period(projectRequestDto.getPeriod())
                .stack(projectRequestDto.getStack())
                .meetingPlan(projectRequestDto.getMeetingPlan())
                .hashtag(projectRequestDto.getHashtag())
                .build();
    }

    /**
     * 기본 이미지 선택 시 : imageRepository 에서 가져옴
     * 새 이미지 선택 시 : 새 객체 생성 후 저장
     */
    private Image selectOrSaveImage(Long imageId, ImageRequestDto imageRequestDto) {
        Image image;
        if (imageRequestDto.getImageUrl().isEmpty()) {
            image = imageRepository.getReferenceById(imageId);
        } else {
            image = new Image(imageRequestDto.getName(), imageRequestDto.getImageUrl());
            imageRepository.save(image);
        }
        return image;
    }
}
