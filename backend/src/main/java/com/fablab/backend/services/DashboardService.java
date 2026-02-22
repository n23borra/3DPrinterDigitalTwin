package com.fablab.backend.services;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fablab.backend.dto.DashboardCountsDTO;
import com.fablab.backend.models.Alert;
import com.fablab.backend.models.printer.Printer;
import com.fablab.backend.models.printer.PrinterStatus;
import com.fablab.backend.repositories.AlertRepository;
import com.fablab.backend.repositories.AuditLogRepository;
import com.fablab.backend.repositories.UserRepository;
import com.fablab.backend.repositories.printer.PrinterRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final AlertRepository alertRepository;
    private final PrinterRepository printerRepository;

    public DashboardCountsDTO getCounts() {
        long userCount = userRepository.count();
        /*long alertCount = auditLogRepository.countByActionContainingIgnoreCase("FAILED")
                + auditLogRepository.countByActionContainingIgnoreCase("ERROR")
                + auditLogRepository.countByActionContainingIgnoreCase("ALERT");*/
        long alertCount = alertRepository.findByStatus(Alert.Status.UNRESOLVED).size();
        
        // Availability rule: a printer is considered available when its persisted status is IDLE.
        long printerAvailableCount = printerRepository.countByStatus(PrinterStatus.IDLE);

        return new DashboardCountsDTO(userCount, alertCount, printerAvailableCount);
    }

    public List<Alert> getAlertsForPrinter(UUID printerId){
        return alertRepository.findByPrinterId(printerId);
    }

    public List<UUID> getAllUuids(){
        List<UUID> ids = printerRepository.findAll().stream().map(Printer::getId).toList();
        return ids;
    }
}