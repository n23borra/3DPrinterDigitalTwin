package com.fablab.twin.repository;

import com.fablab.twin.domain.model.ErrorEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ErrorEventRepository extends JpaRepository<ErrorEvent, UUID> {
    List<ErrorEvent> findTop50ByOrderByCreatedAtDesc();
    List<ErrorEvent> findByCreatedAtAfter(Instant timestamp);
}