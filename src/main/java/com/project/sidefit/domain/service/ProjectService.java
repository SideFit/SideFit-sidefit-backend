package com.project.sidefit.domain.service;

import com.project.sidefit.domain.entity.*;
import com.project.sidefit.domain.repository.ApplyRepository;
import com.project.sidefit.domain.repository.BookmarkRepository;
import com.project.sidefit.domain.repository.ImageRepository;
import com.project.sidefit.domain.repository.UserJpaRepo;
import com.project.sidefit.domain.repository.project.ProjectRepository;
import com.project.sidefit.domain.repository.project.ProjectUserRepository;
import com.project.sidefit.domain.repository.project.KeywordRepository;
import com.project.sidefit.domain.repository.project.RecruitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.project.sidefit.api.dto.ProjectDto.*;

@Service
@Transactional
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectUserRepository projectUserRepository;
    private final RecruitRepository recruitRepository;
    private final UserJpaRepo userRepository;
    private final ImageRepository imageRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ApplyRepository applyRepository;
    private final KeywordRepository keywordRepository;

    public Long makeProject(Long userId, Long imageId, ProjectRequestDto projectRequestDto) {
        User user = userRepository.getReferenceById(userId);
        Image image = selectOrSaveImage(imageId, projectRequestDto);
        Project project = createProject(projectRequestDto, user, image);
        ProjectUser projectUser = ProjectUser.createProjectUser(user, project);

        for (RecruitRequestDto dto : projectRequestDto.getRecruits()) {
            Recruit recruit = Recruit.create(project, dto.getJobGroup(), dto.getRecruitNumber());
            recruitRepository.save(recruit);
        }
        projectUserRepository.save(projectUser);

        String[] hashtags = project.getHashtag().split("#");
        for (String hashtag : hashtags) {
            Optional<Keyword> recommendKeywordOptional = keywordRepository.findByWord(hashtag);
            if (recommendKeywordOptional.isPresent()) {
                recommendKeywordOptional.get().addCount();
            } else {
                Keyword keyword = new Keyword(hashtag, 1);
                keywordRepository.save(keyword);
            }
        }
        return projectRepository.save(project).getId();
    }

    // TODO: 어떤 필드를 수정하는지?
    public void updateProject(Long projectId, Long imageId, ProjectRequestDto projectRequestDto) {
        Project project = findProject(projectId);
        if (!project.isStatus()) {
            throw new IllegalStateException("이미 종료된 프로젝트입니다.");
        }
        Image image = selectOrSaveImage(imageId, projectRequestDto);
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
        findProject(projectId).end();
    }

    // TODO: projectId 를 FK 로 가지고 있는 엔티티 처리 -> Bookmark, Apply, Chat
    public void deleteProject(Long projectId) {
        Project project = findProject(projectId);
        bookmarkRepository.findAll().stream().filter(bookmark -> bookmark.getProject().equals(project)).forEach(bookmarkRepository::delete);
        applyRepository.findAll().stream().filter(apply -> apply.getProject().equals(project)).forEach(applyRepository::delete);
        projectRepository.delete(project); // orphan remove : ProjectUser, Recruit
    }

    @Transactional(readOnly = true)
    public List<MemberResponseDto> findProjectUserDtoListWithProjectId(Long projectId) {
        Project project = findProject(projectId);
        return projectUserRepository.findMembers(project.getId());
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
        scoreMapEntry.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        return scoreMapEntry.stream()
                .map(entry -> new ProjectRecommendDto(entry.getKey()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<KeywordResponseDto> findRecommendKeywordDtoList() {
        return keywordRepository.findTop10OrderByCountDesc().stream()
                .map(KeywordResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectResponseDto findProjectDto(Long projectId) {
        return new ProjectResponseDto(findProject(projectId));
    }

    @Transactional(readOnly = true)
    public KeywordResponseDto findKeywordDto(Long keywordId) {
        return new KeywordResponseDto(findKeyword(keywordId));
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

    @Transactional(readOnly = true)
    private Keyword findKeyword(Long keywordId) {
        return keywordRepository.findById(keywordId)
                .orElseThrow(() -> new IllegalStateException("This keyword is null: " + keywordId));
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
    private Image selectOrSaveImage(Long imageId, ProjectRequestDto projectRequestDto) {
        Image image;
        if (projectRequestDto.getImageUrl().isEmpty()) {
            image = imageRepository.getReferenceById(imageId);
        } else {
            image = new Image(projectRequestDto.getName(), projectRequestDto.getImageUrl());
            imageRepository.save(image);
        }
        return image;
    }
}
