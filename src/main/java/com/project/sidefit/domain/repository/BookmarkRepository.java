package com.project.sidefit.domain.repository;

import com.project.sidefit.domain.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
}
