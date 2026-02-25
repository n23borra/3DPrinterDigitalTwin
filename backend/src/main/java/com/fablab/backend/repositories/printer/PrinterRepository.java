package com.fablab.backend.repositories.printer;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fablab.backend.models.printer.Printer;
import com.fablab.backend.models.printer.PrinterStatus;

public interface PrinterRepository extends JpaRepository<Printer, UUID> {
    long countByStatus(PrinterStatus status);
}