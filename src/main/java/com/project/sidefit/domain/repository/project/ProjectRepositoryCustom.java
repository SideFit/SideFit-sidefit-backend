package com.project.sidefit.domain.repository.project;

import java.util.List;

import static com.project.sidefit.api.dto.ProjectDto.*;

public interface ProjectRepositoryCustom {

    List<ProjectQueryDto> searchProject(String keyword);
}
