package com.goose.backend.repository;

import com.goose.backend.model.Slide;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SlideRepository extends JpaRepository<Slide,Long> {
    Page<Slide> findByGroup_IdOrderByIdAsc(Long groupId, Pageable pageable);
    Page<Slide> findByGroup_Theme_Owner_IdAndTitleIgnoreCaseOrderByIdAsc(Long ownerId, String title, Pageable pageable);
    Page<Slide> findByGroup_IdAndTitleContainingIgnoreCaseOrderByIdAsc(Long groupId, String title, Pageable pageable);
}
