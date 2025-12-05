package com.fablab.backend.repositories.printer;

import com.fablab.backend.models.printer.Printer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PrinterRepository extends JpaRepository<Printer, UUID> {
}