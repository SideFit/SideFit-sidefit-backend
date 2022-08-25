package com.project.sidefit.domain.repository.project;

import com.project.sidefit.api.dto.QProjectDto_ProjectQueryDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.project.sidefit.api.dto.ProjectDto.*;
import static com.project.sidefit.domain.entity.QImage.*;
import static com.project.sidefit.domain.entity.QProject.*;
import static com.project.sidefit.domain.entity.QRecruit.*;

@Repository
@RequiredArgsConstructor
public class ProjectRepositoryImpl implements ProjectRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ProjectQueryDto> searchProject(String keyword) {
        List<ProjectQueryDto> projects = queryFactory
                .select(new QProjectDto_ProjectQueryDto(
                        project.id,
                        project.title,
                        project.type,
                        project.hashtag,
                        project.status,
                        project.createdDate,
                        project.lastModifiedDate,
                        image.id,
                        image.imageUrl
                ))
                .from(project)
                .join(project.image, image)
                .where(project.title.contains(keyword)
                        .or(project.hashtag.contains(keyword)))
                .orderBy(project.createdDate.desc())
                .fetch();

        List<Long> projectIds = projects.stream()
                .map(ProjectQueryDto::getId)
                .collect(Collectors.toList());

        List<RecruitResponseDto> recruits = queryFactory
                .select(Projections.constructor(
                        RecruitResponseDto.class,
                        recruit.id,
                        project.id,
                        recruit.jobGroup,
                        recruit.currentNumber,
                        recruit.recruitNumber
                ))
                .from(recruit)
                .join(recruit.project, project)
                .where(project.id.in(projectIds))
                .fetch();

        Map<Long, List<RecruitResponseDto>> recruitMap = recruits.stream()
                .collect(Collectors.groupingBy(RecruitResponseDto::getProjectId));
        projects.forEach(projectQueryDto -> projectQueryDto.setRecruits(recruitMap.get(projectQueryDto.getId())));

        return projects;
    }
}
