package com.project.sidefit.domain.repository.project;

import com.project.sidefit.domain.enums.SearchCondition;

import java.util.List;

import static com.project.sidefit.api.dto.ProjectDto.*;

public interface ProjectRepositoryCustom {

    List<ProjectQueryDto> searchProject(String keyword); // 프로젝트 검색 (최신순)
    List<ProjectQueryDto> searchProjectByKeywords(List<String> jobGroups, List<String> fields, List<String> periods, List<Integer> types, SearchCondition condition); // 키워드 선택하여 검색
}