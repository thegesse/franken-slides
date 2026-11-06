package com.goose.backend.repository;

import com.goose.backend.model.Theme;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ThemeRepository  extends JpaRepository<Theme, Long> {
    Page<Theme> findByOwnerId(Long ownerId, Pageable pageable);
    Optional<Theme> findByOwnerId(Long ownerId);
}
