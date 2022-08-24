package com.project.sidefit.domain.repository.project;

import com.project.sidefit.domain.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long>, ProjectRepositoryCustom {
}
