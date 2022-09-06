package com.project.sidefit.domain.repository;

import com.project.sidefit.domain.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {

    long deleteByImageUrl(String imageUrl);
}
