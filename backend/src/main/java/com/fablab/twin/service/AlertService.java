package com.fablab.twin.service;

import com.fablab.twin.domain.model.ErrorEvent;
import com.fablab.twin.domain.model.ErrorSeverity;
import com.fablab.twin.domain.model.ErrorType;
import com.fablab.twin.domain.model.Printer;
import com.fablab.twin.repository.ErrorEventRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AlertService {

    private final ErrorEventRepository errorEventRepository;
    private final PrinterService printerService;

    public AlertService(ErrorEventRepository errorEventRepository, PrinterService printerService) {
        this.errorEventRepository = errorEventRepository;
        this.printerService = printerService;
    }

    public ErrorEvent create(UUID printerId, ErrorSeverity severity, ErrorType type, String message, String details) {
        Printer printer = printerService.findAll().stream()
                .filter(p -> p.getId().equals(printerId))
                .findFirst()
                .orElseThrow();
        ErrorEvent event = ErrorEvent.builder()
                .printer(printer)
                .severity(severity)
                .type(type)
                .message(message)
                .details(details)
                .acknowledged(false)
                .createdAt(Instant.now())
                .build();
        return errorEventRepository.save(event);
    }

    public List<ErrorEvent> listRecent() {
        return errorEventRepository.findTop50ByOrderByCreatedAtDesc();
    }

    public void acknowledge(UUID id) {
        errorEventRepository.findById(id).ifPresent(event -> {
            event.setAcknowledged(true);
            errorEventRepository.save(event);
        });
    }
}