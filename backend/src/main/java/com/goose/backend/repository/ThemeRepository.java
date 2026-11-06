package com.goose.backend.repository;

import com.goose.backend.model.Theme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThemeRepository  extends JpaRepository<Theme, Long> {
}
