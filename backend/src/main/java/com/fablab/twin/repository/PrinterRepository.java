package com.fablab.twin.repository;

import com.fablab.twin.domain.model.Printer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PrinterRepository extends JpaRepository<Printer, UUID> {
}