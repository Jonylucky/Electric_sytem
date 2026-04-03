package com.index_electric_server.reading.controller;

import com.index_electric_server.reading.dto.location.LocationDto;
import com.index_electric_server.reading.service.location.LocationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    public List<LocationDto> getAll() {
        return locationService.getAll();
    }
}