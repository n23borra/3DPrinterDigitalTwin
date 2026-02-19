package com.fablab.backend.services;

import com.fablab.backend.dto.DashboardCountsDTO;
import com.fablab.backend.models.printer.PrinterStatus;
import com.fablab.backend.repositories.AuditLogRepository;
import com.fablab.backend.repositories.UserRepository;
import com.fablab.backend.repositories.printer.PrinterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final PrinterRepository printerRepository;

    public DashboardCountsDTO getCounts() {
        long userCount = userRepository.count();
        long alertCount = auditLogRepository.countByActionContainingIgnoreCase("FAILED")
                + auditLogRepository.countByActionContainingIgnoreCase("ERROR")
                + auditLogRepository.countByActionContainingIgnoreCase("ALERT");

        // Availability rule: a printer is considered available when its persisted status is IDLE.
        long printerAvailableCount = printerRepository.countByStatus(PrinterStatus.IDLE);

        return new DashboardCountsDTO(userCount, alertCount, printerAvailableCount);
    }
}