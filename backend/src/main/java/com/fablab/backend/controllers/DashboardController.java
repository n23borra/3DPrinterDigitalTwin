package com.fablab.backend.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fablab.backend.dto.DashboardCountsDTO;
import com.fablab.backend.models.Alert;
import com.fablab.backend.models.printer.Printer;
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
    public Map<String, List<Alert>> getAlertsByPrinter(){
        HashMap<String, List<Alert>> map = new HashMap<>();
        List<Printer> printers = dashboardService.getAllPrinters();
        for(Printer printer : printers){
            List<Alert> alerts = dashboardService.getAlertsForPrinter(printer.getId());
            map.put(printer.getName(), alerts);
        }
        return map;
    }
}