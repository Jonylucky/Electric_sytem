package com.index_electric_server.reading.service;


import com.index_electric_server.common.enums.AlertLevel;
import com.index_electric_server.common.exception.BadRequestException;
import com.index_electric_server.common.util.MonthUtil;
import com.index_electric_server.reading.dto.*;
import com.index_electric_server.reading.dto.report.ExportReadingRowDto;
import com.index_electric_server.reading.entity.Reading;
import com.index_electric_server.reading.repository.ReadingRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.time.*;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;

@Service
public class ReadingSyncService {

    private final ReadingRepository readingRepository;
    private final StorageService storageService;

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final int DEFAULT_CUTOFF_DAY = 20;

    public ReadingSyncService(ReadingRepository readingRepository, StorageService storageService) {
        this.readingRepository = readingRepository;
        this.storageService = storageService;
    }

    private String sanitizeKey(String raw) {
        return raw.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    private String imageKey(String meterId, String month) {
        return sanitizeKey(meterId + "_" + month);
    }
    private String previousMonth(String month) {
        java.time.YearMonth ym = java.time.YearMonth.parse(month);
        return ym.minusMonths(1).toString();
    }

    private AlertLevel calculateAlertLevel(String meterId, String month, int currentConsumption) {
        String prevMonth = previousMonth(month);

        Optional<Reading> previousOpt =
                readingRepository.findByMeterIdAndMonthAndReadingType(
                        meterId,
                        prevMonth,
                        Reading.ReadingType.OFFICIAL
                );

        if (previousOpt.isEmpty()) {
            return AlertLevel.NORMAL;
        }

        Integer previousConsumption = previousOpt.get().getIndexConsumption();
        if (previousConsumption == null || previousConsumption <= 0) {
            return AlertLevel.NORMAL;
        }

        double percent = ((currentConsumption - previousConsumption) * 100.0) / previousConsumption;

        if (percent >= 30.0) {
            return AlertLevel.DANGER;
        }
        if (percent >= 20.0) {
            return AlertLevel.WARNING;
        }
        return AlertLevel.NORMAL;
    }
    // ===== Phase 1 =====
    @Transactional
    public SyncMonthResult syncMonthData(List<SyncPayload> items) {
        if (items == null || items.isEmpty()) throw new BadRequestException("items is required");

        List<SyncMonthResultRow> rows = new ArrayList<>();

        for (int idx = 0; idx < items.size(); idx++) {
            SyncPayload p = items.get(idx);
            try {
                SyncResponse r = syncRecordOnly(p);

                SyncMonthResultRow ok = new SyncMonthResultRow();
                ok.setI(idx);
                ok.setMeterId(p.getMeterId());
                ok.setMonth(r.getResult().getMonth());
                ok.setStatus(r.getResult().getStatus());
                ok.setReadingId(r.getResult().getReadingId());
                ok.setSuccess(true);
                rows.add(ok);

            } catch (Exception ex) {
                SyncMonthResultRow fail = new SyncMonthResultRow();
                fail.setI(idx);
                fail.setMeterId(p.getMeterId());
                fail.setMonth(p.getMonth());
                fail.setSuccess(false);
                fail.setError(ex.getMessage());
                rows.add(fail);
            }
        }

        SyncMonthResult resp = new SyncMonthResult();
        resp.setResults(rows);
        resp.setCount(rows.size());
        resp.setSuccess(!rows.isEmpty() && rows.stream().allMatch(r -> Boolean.TRUE.equals(r.getSuccess())));
        return resp;
    }

    // upsert record, không ảnh
    @Transactional
    public SyncResponse syncRecordOnly(SyncPayload payload) {
        if (payload.getMeterId() == null || payload.getMeterId().isBlank()) {
            throw new BadRequestException("meterId is required");
        }
        String meterId = payload.getMeterId().trim();
        String month = payload.getMonth();

        if (month == null || month.isBlank()) {
            throw new BadRequestException("month is required");
        }

        Optional<Reading> existingOpt =
                readingRepository.findByMeterIdAndMonthAndReadingType(
                        meterId, month, Reading.ReadingType.OFFICIAL
                );

        boolean created = existingOpt.isEmpty();
        Reading r = existingOpt.orElseGet(Reading::new);

        if (created) {
            r.setMeterId(meterId);
            r.setMonth(month);
            r.setReadingType(Reading.ReadingType.OFFICIAL);
        }

        Integer prev = payload.getIndexPrevMonth();
        Integer last = payload.getIndexLastMonth();

        if (prev == null) {
            throw new BadRequestException("indexPrevMonth is required");
        }
        if (last == null) {
            throw new BadRequestException("indexLastMonth is required");
        }

        int consumption = last - prev;
        if (consumption < 0) {
            throw new BadRequestException("indexLastMonth must be >= indexPrevMonth");
        }

        r.setIndexPrevMonth(prev);
        r.setIndexLastMonth(last);
        r.setIndexConsumption(consumption);

        AlertLevel alertLevel = calculateAlertLevel(meterId, month, consumption);
        r.setAlertLevel(alertLevel);

        if (payload.getCapturedAt() != null) {
            r.setCapturedAt(payload.getCapturedAt());
        }

        r.setImageKey(imageKey(meterId, month));

        Reading saved = readingRepository.save(r);

        SyncResultItem result = new SyncResultItem();
        result.setMeterId(meterId);
        result.setMonth(month);
        result.setStatus(created ? "CREATED" : "UPDATED");
        result.setReadingId(saved.getReadingId());

        SyncResponse resp = new SyncResponse();
        resp.setSuccess(true);
        resp.setMessage(created ? "Created official reading" : "Updated official reading");
        resp.setResult(result);
        return resp;
    }

    // ===== Phase 2 =====
    @Transactional
    public SyncMonthResult attachImagesFromZip(MultipartFile zipFile, int batch) throws Exception {
        if (zipFile == null || zipFile.isEmpty()) throw new BadRequestException("zip file is required");

        List<SyncMonthResultRow> rows = new ArrayList<>();

        try (var zis = new java.util.zip.ZipInputStream(zipFile.getInputStream())) {
            java.util.zip.ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;

                String fullName = entry.getName(); // "VT_7_T7_CT1_2026-02.jpg"
                String base = fullName.contains("/") ? fullName.substring(fullName.lastIndexOf('/') + 1) : fullName;

                String stem = base;
                int dot = stem.lastIndexOf('.');
                if (dot > 0) stem = stem.substring(0, dot);

                String key = stem; // ✅ imageKey

                try {
                    byte[] bytes = zis.readAllBytes();

                    Reading target = readingRepository.findOfficialByImageKey(key)
                            .orElseThrow(() -> new BadRequestException("No OFFICIAL reading for imageKey=" + key));

                    String fileName = key + ".jpg";
                    String url = storageService.saveImageBytes(bytes, target.getMonth(), fileName);
                    readingRepository.updateImageUrl(target.getReadingId(), url);

                    SyncMonthResultRow ok = new SyncMonthResultRow();
                    ok.setSuccess(true);
                    ok.setMeterId(target.getMeterId());
                    ok.setMonth(target.getMonth());
                    ok.setReadingId(target.getReadingId());
                    ok.setStatus("IMAGE_UPDATED");
                    rows.add(ok);

                } catch (Exception ex) {
                    SyncMonthResultRow fail = new SyncMonthResultRow();
                    fail.setSuccess(false);
                    fail.setError("batch=" + batch + " file=" + base + " err=" + ex.getMessage());
                    rows.add(fail);
                }
            }
        }

        SyncMonthResult resp = new SyncMonthResult();
        resp.setResults(rows);
        resp.setCount(rows.size());
        resp.setSuccess(!rows.isEmpty() && rows.stream().allMatch(r -> Boolean.TRUE.equals(r.getSuccess())));
        return resp;
    }
    @Transactional
    public PullMonthResponse pullOfficialMonth(String month) {
        if (month == null || month.isBlank()) {
            throw new BadRequestException("month is required");
        }

        List<Reading> readings = readingRepository
                .findByMonthAndReadingTypeOrderByMeterIdAsc(
                        month,
                        Reading.ReadingType.OFFICIAL
                );

        List<ReadingPullItem> items = new ArrayList<>();

        for (Reading r : readings) {
            ReadingPullItem item = new ReadingPullItem();
            item.setMeterId(r.getMeterId());
            item.setMonth(r.getMonth());
            item.setIndexPrevMonth(r.getIndexPrevMonth());
            item.setIndexLastMonth(r.getIndexLastMonth());
            item.setIndexConsumption(r.getIndexConsumption());
            item.setCreatedAt(r.getCapturedAt());
            items.add(item);
        }
        System.out.println("PULL month=" + month + " total=" + readings.size());
        for (Reading r : readings) {
            System.out.println("meter=" + r.getMeterId()
                    + ", month=" + r.getMonth()
                    + ", type=" + r.getReadingType());
        }
        PullMonthResponse resp = new PullMonthResponse();
        resp.setSuccess(true);
        resp.setMonth(month);
        resp.setCount(items.size());
        resp.setItems(items);
        return resp;
    }
    public List<ExportReadingRowDto> getReadingsByMonth(String month, Long companyId) {
        if (companyId == null) {
            return readingRepository.findExportRowsByMonth(month);
        }
        return readingRepository.findExportRowsByMonthAndCompanyId(month, companyId);
    }
}