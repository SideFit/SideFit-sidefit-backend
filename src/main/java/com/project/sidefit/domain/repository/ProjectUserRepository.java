package com.project.sidefit.domain.repository;

import com.project.sidefit.domain.entity.ProjectUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectUserRepository extends JpaRepository<ProjectUser, Long> {
}
