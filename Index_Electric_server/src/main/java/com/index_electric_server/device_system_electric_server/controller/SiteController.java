package com.index_electric_server.device_system_electric_server.controller;

import com.index_electric_server.device_system_electric_server.dto.SiteDto;
import com.index_electric_server.device_system_electric_server.service.SiteService;
import com.index_electric_server.device_system_electric_server.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sites")
@RequiredArgsConstructor
public class SiteController {

    private final SiteService siteService;

    @PostMapping
    public ResponseEntity<ApiResponse<SiteDto.Response>> create(@RequestBody SiteDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(siteService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SiteDto.Response>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(siteService.findById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SiteDto.Response>>> findAll() {
        return ResponseEntity.ok(ApiResponse.ok(siteService.findAll()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SiteDto.Response>> update(
            @PathVariable Long id,
            @RequestBody SiteDto.Request request) {
        return ResponseEntity.ok(ApiResponse.ok("Updated", siteService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        siteService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
