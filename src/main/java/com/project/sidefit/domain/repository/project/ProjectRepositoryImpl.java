package com.project.sidefit.domain.repository.project;

import com.project.sidefit.api.dto.QProjectDto_ProjectQueryDto;
import com.project.sidefit.domain.enums.SearchCondition;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.*;
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
                        project.field,
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
                        .or(project.introduction.contains(keyword))
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

    @Override
    public List<ProjectQueryDto> searchProjectByKeywords(List<String> jobGroups, List<String> fields, List<String> periods, List<Integer> types, SearchCondition condition) {
        List<ProjectQueryDto> projects = queryFactory
                .select(new QProjectDto_ProjectQueryDto(
                        project.id,
                        project.title,
                        project.type,
                        project.field,
                        project.hashtag,
                        project.status,
                        project.createdDate,
                        project.lastModifiedDate,
                        image.id,
                        image.imageUrl
                ))
                .from(project)
                .join(project.image, image)
                .where(inFields(fields))
                .where(inPeriods(periods))
                .where(inTypes(types))
                .orderBy(byCondition(condition))
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
                .where(inJobGroups(jobGroups))
                .fetch();

        Map<Long, List<RecruitResponseDto>> recruitMap = recruits.stream()
                .collect(Collectors.groupingBy(RecruitResponseDto::getProjectId));
        projects.forEach(projectQueryDto -> projectQueryDto.setRecruits(recruitMap.get(projectQueryDto.getId())));
        projects.removeIf(projectQueryDto -> projectQueryDto.getRecruits() == null);

        return projects;
    }

    private BooleanExpression inFields(List<String> fields) {
        return !fields.isEmpty() ? project.field.in(fields) : null;
    }

    private BooleanExpression inPeriods(List<String> periods) {
        return !periods.isEmpty() ? project.period.in(periods) : null;
    }

    private BooleanExpression inTypes(List<Integer> types) {
        return !types.isEmpty() ? project.type.in(types) : null;
    }

    private BooleanExpression inJobGroups(List<String> jobGroups) {
        return !jobGroups.isEmpty() ? recruit.jobGroup.in(jobGroups) : null;
    }

    private OrderSpecifier<?> byCondition(SearchCondition condition) {
        if (condition == SearchCondition.LATEST) {
            return project.createdDate.desc();
        }
        if (condition == SearchCondition.POPULARITY) {
            return project.id.asc(); // TODO : 인기순 정렬
        }
        return null;
    }
}