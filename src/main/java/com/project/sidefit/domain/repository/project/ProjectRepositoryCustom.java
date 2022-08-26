package com.project.sidefit.domain.repository.project;

import java.util.List;

import static com.project.sidefit.api.dto.ProjectDto.*;

public interface ProjectRepositoryCustom {

    List<ProjectQueryDto> searchProjectByLatestOrder(String keyword); // 최신 순
    List<ProjectQueryDto> searchProjectByAccuracyOrder(String keyword); // 정확도 순
}
