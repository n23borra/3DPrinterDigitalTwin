package com.fablab.twin.repository;

import com.fablab.twin.domain.model.PrintJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PrintJobRepository extends JpaRepository<PrintJob, UUID> {
}