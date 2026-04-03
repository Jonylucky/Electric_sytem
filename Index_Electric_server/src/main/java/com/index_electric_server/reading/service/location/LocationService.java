package com.index_electric_server.reading.service.location;

import com.index_electric_server.reading.dto.location.LocationDto;
import com.index_electric_server.reading.entity.Location;
import com.index_electric_server.reading.repository.LocationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationService {

    private final LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public List<LocationDto> getAll() {
        return locationRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    private LocationDto toDto(Location location) {
        return new LocationDto(
                location.getLocationId(),
                location.getLocationName(),
                location.getFloor()
        );
    }
}