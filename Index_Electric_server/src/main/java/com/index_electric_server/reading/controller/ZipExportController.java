package com.index_electric_server.reading.controller;

import com.index_electric_server.reading.service.export.ZipExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/export")
public class ZipExportController {

    private final ZipExportService zipExportService;

    public ZipExportController(ZipExportService zipExportService) {
        this.zipExportService = zipExportService;
    }

    @GetMapping("/company-report-zip")
    public ResponseEntity<byte[]> exportZip(
            @RequestParam String month,
            @RequestParam Long companyId
    ) throws Exception {

        byte[] zipBytes = zipExportService.exportZipByMonthAndCompanyId(month, companyId);

        String fileName = "bao_cao_" + companyId + "_" + month + ".zip";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(zipBytes);
    }
}