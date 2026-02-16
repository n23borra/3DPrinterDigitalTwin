package com.fablab.backend.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AlertModuleInitializer {

    private final AlertModuleService alertModuleService;
    private static final Logger log = LoggerFactory.getLogger(AlertModuleInitializer.class);

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        Thread t = new Thread(() -> {
            try {
                alertModuleService.startAlertModule();
            } catch (Exception e) {
                log.error("Alert module failed to start", e);
            }
        }, "alert-module-starter");
        t.setDaemon(true);
        t.start();
        log.info("Alert module startup thread started.");
    }
}
