package com.index_electric_server.reading.controller;

import com.index_electric_server.reading.dto.AlertResponse;
import com.index_electric_server.reading.service.report.AlertService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping("/all")
    public AlertResponse checkAllAlerts(
            @RequestParam String month
    ) {
        return alertService.checkAllAlerts(month);
    }
}
