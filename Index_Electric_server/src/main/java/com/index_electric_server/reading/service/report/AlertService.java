package com.index_electric_server.reading.service.report;

import com.index_electric_server.reading.dto.AlertReadingDto;
import com.index_electric_server.reading.dto.AlertResponse;
import com.index_electric_server.reading.repository.AlertReadingProjection;
import com.index_electric_server.reading.repository.ReadingRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AlertService {

    private final ReadingRepository readingRepository;

    public AlertService(ReadingRepository readingRepository) {
        this.readingRepository = readingRepository;
    }

    public AlertResponse checkAllAlerts(String month) {

        List<AlertReadingProjection> rows =
                readingRepository.findAllAlertsByMonth(month);

        List<AlertReadingDto> items = new ArrayList<>();

        int warning = 0;
        int danger = 0;

        for (AlertReadingProjection r : rows) {

            if ("WARNING".equals(r.getAlertLevel())) {
                warning++;
            } else if ("DANGER".equals(r.getAlertLevel())) {
                danger++;
            }

            AlertReadingDto dto = new AlertReadingDto();
            dto.setReadingId(r.getReadingId());
            dto.setCompanyId(r.getCompanyId());
            dto.setCompanyName(r.getCompanyName());
            dto.setMeterId(r.getMeterId());
            dto.setMeterName(r.getMeterName());
            dto.setMonth(r.getMonth());
            dto.setIndexConsumption(r.getIndexConsumption());
            dto.setAlertLevel(r.getAlertLevel());
            dto.setImageUrl(r.getImageUrl());

            items.add(dto);
        }

        AlertResponse resp = new AlertResponse();
        resp.setMonth(month);
        resp.setHasAlert(!items.isEmpty());
        resp.setTotalAlerts(items.size());
        resp.setTotalWarning(warning);
        resp.setTotalDanger(danger);
        resp.setReadings(items);

        return resp;
    }
}