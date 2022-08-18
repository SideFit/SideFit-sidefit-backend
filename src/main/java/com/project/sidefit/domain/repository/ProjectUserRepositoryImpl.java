package com.project.sidefit.domain.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

import static com.project.sidefit.api.dto.ProjectDto.*;
import static com.project.sidefit.domain.entity.QImage.*;
import static com.project.sidefit.domain.entity.QProject.*;
import static com.project.sidefit.domain.entity.QProjectUser.*;
import static com.project.sidefit.domain.entity.QUser.*;

@Repository
@RequiredArgsConstructor
public class ProjectUserRepositoryImpl implements ProjectUserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<MemberResponseDto> findPreMembers(List<Long> projectIds) {
        List<ProjectUserResponseDto> projectUsers = queryFactory
                .select(Projections.constructor(
                        ProjectUserResponseDto.class,
                        projectUser.id,
                        user.id,
                        project.id
                ))
                .from(projectUser)
                .join(projectUser.user, user)
                .join(projectUser.project, project)
                .where(project.id.in(projectIds)
                        .and(project.status.isFalse()) // 현재 진행중인 프로젝트 제외
                )
                .fetch();

        List<Long> userIds = projectUsers.stream().distinct() // userId 중복 제외
                .map(ProjectUserResponseDto::getUserId)
                .collect(Collectors.toList());

        return queryFactory
                .select(Projections.constructor(
                        MemberResponseDto.class,
                        user.id,
                        image.id,
                        user.nickname,
                        user.job
                ))
                .from(user)
                .join(user.image, image)
                .where(user.id.in(userIds))
                .orderBy(user.createdDate.desc())
                .fetch();
    }
}
