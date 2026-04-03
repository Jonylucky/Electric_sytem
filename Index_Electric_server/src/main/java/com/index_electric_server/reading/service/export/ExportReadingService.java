package com.index_electric_server.reading.service.export;


import com.index_electric_server.reading.dto.report.ExportReadingRowDto;
import com.index_electric_server.reading.repository.ReadingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExportReadingService {

    private final ReadingRepository readingRepository;

    public ExportReadingService(ReadingRepository readingRepository) {
        this.readingRepository = readingRepository;
    }

    public List<ExportReadingRowDto> getExportRowsByMonth(String month) {
        return readingRepository.findExportRowsByMonth(month);
    }

    public List<ExportReadingRowDto> getExportRowsByMonthAndCompanyId(String month, Long companyId) {
        return readingRepository.findExportRowsByMonthAndCompanyId(month, companyId);
    }

    public boolean hasDataByMonth(String month) {
        return !readingRepository.findExportRowsByMonth(month).isEmpty();
    }

    public boolean hasDataByMonthAndCompanyId(String month, Long companyId) {
        return !readingRepository.findExportRowsByMonthAndCompanyId(month, companyId).isEmpty();
    }

}
