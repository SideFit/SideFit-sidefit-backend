package com.project.sidefit.domain.repository;

import com.project.sidefit.domain.entity.Apply;
import com.project.sidefit.domain.entity.Project;
import com.project.sidefit.domain.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplyRepository extends JpaRepository<Apply, Long> {

    List<Apply> findByUser(User user);
    List<Apply> findByProject(Project project);
}