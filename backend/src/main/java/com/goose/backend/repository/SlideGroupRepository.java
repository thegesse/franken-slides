package com.goose.backend.repository;

import com.goose.backend.model.SlideGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SlideGroupRepository extends JpaRepository<SlideGroup,Long> {
    Page<SlideGroup> findByTheme_IdOrderByIdAsc(Long themeId, Pageable pageable);
}
