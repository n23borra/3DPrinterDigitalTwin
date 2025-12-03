package com.fablab.twin.service;

import com.fablab.twin.domain.model.ErrorEvent;
import com.fablab.twin.domain.model.PrinterSnapshot;
import com.fablab.twin.repository.ErrorEventRepository;
import com.fablab.twin.repository.PrinterSnapshotRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MetricsService {

    private final PrinterSnapshotRepository snapshotRepository;
    private final ErrorEventRepository errorEventRepository;

    public MetricsService(PrinterSnapshotRepository snapshotRepository, ErrorEventRepository errorEventRepository) {
        this.snapshotRepository = snapshotRepository;
        this.errorEventRepository = errorEventRepository;
    }

    public Map<String, Object> summarizeLastDay() {
        Instant since = Instant.now().minus(1, ChronoUnit.DAYS);
        List<PrinterSnapshot> snapshots = snapshotRepository.findByTimestampAfter(since);
        List<ErrorEvent> errors = errorEventRepository.findByCreatedAtAfter(since);
        Map<String, Object> payload = new HashMap<>();
        payload.put("snapshots", snapshots.size());
        payload.put("errors", errors.size());
        payload.put("averageBedTemp", snapshots.stream()
                .filter(s -> s.getBedTemp() != null)
                .mapToDouble(PrinterSnapshot::getBedTemp)
                .average().orElse(0));
        return payload;
    }
}