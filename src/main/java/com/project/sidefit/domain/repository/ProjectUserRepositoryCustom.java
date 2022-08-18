package com.project.sidefit.domain.repository;

import java.util.List;

import static com.project.sidefit.api.dto.ProjectDto.*;

public interface ProjectUserRepositoryCustom {

    List<MemberResponseDto> findPreMembers(List<Long> projectIds);
}
