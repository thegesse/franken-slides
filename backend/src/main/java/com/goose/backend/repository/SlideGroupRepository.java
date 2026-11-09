package com.goose.backend.repository;

import com.goose.backend.model.SlideGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SlideGroupRepository extends JpaRepository<SlideGroup,Long> {
    Page<SlideGroup> findByTheme_IdOrderByIdAsc(Long themeId, Pageable pageable);
    Optional<SlideGroup> findByIdAndTheme_Owner_Id(Long groupId, Long ownerId);
    Optional<SlideGroup> findByIdAndTheme_IdAndTheme_Owner_Id(Long groupId, Long themeId, Long ownerId);
}
