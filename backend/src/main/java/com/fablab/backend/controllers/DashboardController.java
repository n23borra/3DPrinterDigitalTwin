package com.fablab.backend.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fablab.backend.dto.DashboardCountsDTO;
import com.fablab.backend.models.Alert;
import com.fablab.backend.services.DashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/counts")
    public DashboardCountsDTO getCounts() {
        return dashboardService.getCounts();
    }

    @GetMapping("/alerts")
    public Map<UUID, List<Alert>> getAlertsByPrinter(){
        HashMap<UUID, List<Alert>> map = new HashMap<>();
        List<UUID> printerIds = dashboardService.getAllUuids();
        for(UUID id : printerIds){
            List<Alert> alerts = dashboardService.getAlertsForPrinter(id);
            map.put(id, alerts);
        }
        System.out.println("MAP = "+map);
        return map;
    }
}