package com.index_electric_server.reading.controller;

import com.index_electric_server.reading.dto.metter.MeterRequest;
import com.index_electric_server.reading.entity.Meter;
import com.index_electric_server.reading.service.Meter.MeterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meters")
public class MeterController {

    private final MeterService meterService;

    public MeterController(MeterService meterService) {
        this.meterService = meterService;
    }

    @GetMapping
    public ResponseEntity<List<Meter>> getMeters(
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) Long locationId
    ) {
        if (companyId != null && locationId != null) {
            return ResponseEntity.ok(
                    meterService.getMetersByCompanyAndLocation(companyId, locationId)
            );
        }

        if (companyId != null) {
            return ResponseEntity.ok(
                    meterService.getMetersByCompanyId(companyId)
            );
        }

        if (locationId != null) {
            return ResponseEntity.ok(
                    meterService.getMetersByLocationId(locationId)
            );
        }

        return ResponseEntity.ok(meterService.getAllMeters());
    }

    @GetMapping("/{meterId}")
    public ResponseEntity<Meter> getMeterById(@PathVariable String meterId) {
        return ResponseEntity.ok(meterService.getMeterById(meterId));
    }
    @PostMapping
    public ResponseEntity<Meter> create(@Valid @RequestBody MeterRequest request) {
        Meter created = meterService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{meterId}")
    public ResponseEntity<Meter> update(
            @PathVariable String meterId,
            @Valid @RequestBody MeterRequest request
    ) {
        Meter updated = meterService.update(meterId, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{meterId}")
    public ResponseEntity<String> delete(@PathVariable String meterId) {
        meterService.delete(meterId);
        return ResponseEntity.ok("Xóa meter thành công: " + meterId);
    }

}