package com.fablab.twin.controller;

import com.fablab.twin.service.MetricsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller for retrieving system metrics.
 * Provides an endpoint to get a summary of metrics for the last day.
 */

@RestController
@RequestMapping("/api/metrics")
public class MetricsController
{

    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService)
    {
        this.metricsService = metricsService;
    }

    @GetMapping("/summary")
    public Map<String, Object> summary()
    {
        return metricsService.summarizeLastDay();
    }
}