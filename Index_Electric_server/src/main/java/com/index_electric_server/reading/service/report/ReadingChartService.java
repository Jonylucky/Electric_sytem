package com.index_electric_server.reading.service.report;


import com.index_electric_server.reading.dto.ReadingChartPointDto;
import com.index_electric_server.reading.dto.ReadingChartResponseDto;
import com.index_electric_server.reading.repository.ReadingChartProjection;
import com.index_electric_server.reading.repository.ReadingRepository;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReadingChartService {

    private final ReadingRepository readingRepository;

    public ReadingChartService(ReadingRepository readingRepository) {
        this.readingRepository = readingRepository;
    }

    public ReadingChartResponseDto getChartByMeterId(String meterId, String month) {
        if (meterId == null || meterId.isBlank()) {
            throw new IllegalArgumentException("meterId is required");
        }
        if (month == null || month.isBlank()) {
            throw new IllegalArgumentException("month is required");
        }

        List<String> months = buildLast4Months(month);

        List<ReadingChartProjection> rows =
                readingRepository.findChartByMeterIdAndMonths(meterId.trim(), months);

        Map<String, ReadingChartProjection> rowMap = new HashMap<>();
        for (ReadingChartProjection row : rows) {
            rowMap.put(row.getMonth(), row);
        }

        List<ReadingChartPointDto> points = new ArrayList<>();

        for (String m : months) {
            ReadingChartProjection row = rowMap.get(m);

            if (row == null) {
                points.add(new ReadingChartPointDto(
                        m,
                        0,
                        0,
                        0,
                        "NO_DATA"
                ));
                continue;
            }

            points.add(new ReadingChartPointDto(
                    row.getMonth(),
                    row.getIndexPrevMonth(),
                    row.getIndexLastMonth(),
                    row.getIndexConsumption(),
                    row.getAlertLevel()
            ));
        }

        return new ReadingChartResponseDto(meterId.trim(), month, points);
    }

    private List<String> buildLast4Months(String month) {
        YearMonth ym = YearMonth.parse(month);

        List<String> result = new ArrayList<>();
        result.add(ym.minusMonths(3).toString());
        result.add(ym.minusMonths(2).toString());
        result.add(ym.minusMonths(1).toString());
        result.add(ym.toString());

        return result;
    }
}
