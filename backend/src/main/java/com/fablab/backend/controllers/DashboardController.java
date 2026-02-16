package com.fablab.backend.controllers;

import com.fablab.backend.dto.DashboardCountsDTO;
import com.fablab.backend.services.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/counts")
    public DashboardCountsDTO getCounts() {
        return dashboardService.getCounts();
    }
}