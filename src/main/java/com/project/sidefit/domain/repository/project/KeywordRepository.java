package com.project.sidefit.domain.repository.project;

import com.project.sidefit.domain.entity.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {

    Optional<Keyword> findByWord(String word);
    List<Keyword> findTop10ByOrderByCountDesc();
}
