package com.index_electric_server.reading.controller;


import com.index_electric_server.reading.dto.ReadingChartResponseDto;
import com.index_electric_server.reading.service.report.ReadingChartService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chart")
public class ReadingChartController {

    private final ReadingChartService readingChartService;

    public ReadingChartController(ReadingChartService readingChartService) {
        this.readingChartService = readingChartService;
    }

    @GetMapping("/consumption")
    public ReadingChartResponseDto getConsumptionChart(
            @RequestParam String meterId,
            @RequestParam String month
    ) {
        return readingChartService.getChartByMeterId(meterId, month);
    }
}