package com.project.sidefit.domain.service;

import com.project.sidefit.domain.entity.*;
import com.project.sidefit.domain.repository.ImageRepository;
import com.project.sidefit.domain.repository.ProjectUserRepository;
import com.project.sidefit.domain.repository.UserJpaRepo;
import com.project.sidefit.domain.repository.project.ProjectRepository;
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

    public void updateProject(Long projectId, ProjectRequestDto projectRequestDto) {
        Project project = findProject(projectId);
        Image image = updateImage(projectRequestDto);
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

    public void endProject(Long projectId) {
        Project project = findProject(projectId);
        project.end();
    }

    // TODO: projectId 를 FK 로 가지고 있는 엔티티 처리
    public void deleteProject(Long projectId) {
        projectRepository.delete(findProject(projectId));
    }

    @Transactional(readOnly = true)
    public List<MemberResponseDto> findProjectUserDtoListWithProjectId(Long projectId) {
        Project project = findProject(projectId);
        List<User> members = project.getProjectUsers().stream()
                .map(ProjectUser::getUser)
                .collect(Collectors.toList());

        return members.stream()
                .map(MemberResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MemberResponseDto> findPreMemberDtoListWithUserId(Long userId) {
        User user = userRepository.getReferenceById(userId);
        List<ProjectUser> projectUserList = projectUserRepository.findByUser(user);
        List<Long> projectIds = projectUserList.stream()
                .map(projectUser -> projectUser.getProject().getId())
                .collect(Collectors.toList());

        return projectUserRepository.findPreMembers(projectIds);
    }

    // TODO: 점수 정렬 기준 수정
    @Transactional(readOnly = true)
    public List<ProjectRecommendDto> findRecommendProjectDtoListWithUserId(Long userId) {
        User user = userRepository.getReferenceById(userId);
        List<Project> projects = projectRepository.findAll();

        // key: project, value: score
        Map<Project, Integer> scoreMap = new HashMap<>();
        for (Project project : projects) {
            int score = project.recommendScoreByUser(user);
            scoreMap.put(project, score);
        }
        // 각각 합산한 점수들을 내림차순으로 나열
        List<Map.Entry<Project, Integer>> scoreMapEntry = new ArrayList<>(scoreMap.entrySet());
        scoreMapEntry.sort(Comparator.comparingInt(Map.Entry::getValue));

        return scoreMapEntry.stream()
                .map(entry -> new ProjectRecommendDto(entry.getKey()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectResponseDto findProjectDto(Long projectId) {
        return new ProjectResponseDto(findProject(projectId));
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
                .status(true)
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
