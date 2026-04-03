package com.index_electric_server.reading.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.index_electric_server.reading.dto.*;
import com.index_electric_server.reading.dto.report.ExportReadingRowDto;
import com.index_electric_server.reading.service.ReadingSyncService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/readings")
public class ReadingSyncController {

    private final ReadingSyncService readingSyncService;

    public ReadingSyncController(ReadingSyncService readingSyncService) {
        this.readingSyncService = readingSyncService;
    }

    // Phase 1: records only
    @PostMapping(
            value = "/sync-month-data",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SyncMonthResult syncMonthData(@RequestBody List<SyncPayload> items) {
        return readingSyncService.syncMonthData(items);
    }

    // Phase 2: upload zip batches
    @PostMapping(
            value = "/records/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SyncMonthResult uploadZip(
            @RequestParam("file") MultipartFile file,
            @RequestParam("batch") int batch
    ) throws Exception {

        System.out.println(">>> HIT /records/images batch=" + batch
                + " file=" + (file != null ? file.getOriginalFilename() : "null")
                + " size=" + (file != null ? file.getSize() : -1));

        return readingSyncService.attachImagesFromZip(file, batch);
    }
    @GetMapping("/pull-month")
    public PullMonthResponse pullMonth(@RequestParam String month) {
        return readingSyncService.pullOfficialMonth(month);
    }

    @GetMapping("/month")
        public List<ExportReadingRowDto> getReadingsByMonthAndCompany(
            @RequestParam String month,
            @RequestParam Long companyId
    ) {
        return readingSyncService.getReadingsByMonth(month, companyId);
    }
}