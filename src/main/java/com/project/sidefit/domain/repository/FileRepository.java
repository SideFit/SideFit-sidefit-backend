package com.project.sidefit.domain.repository;

import com.project.sidefit.domain.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<FileEntity, Long> {
}
