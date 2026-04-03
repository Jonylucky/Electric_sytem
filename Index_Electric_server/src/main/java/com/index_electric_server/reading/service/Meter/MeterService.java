package com.index_electric_server.reading.service.Meter;

import com.index_electric_server.reading.dto.metter.MeterRequest;
import com.index_electric_server.reading.entity.Meter;
import com.index_electric_server.reading.repository.MeterRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MeterService {

    private final MeterRepository meterRepository;

    public MeterService(MeterRepository meterRepository) {
        this.meterRepository = meterRepository;
    }

    public List<Meter> getAllMeters() {
        return meterRepository.findAll();
    }

    public Meter getMeterById(String meterId) {
        return meterRepository.findById(meterId)
                .orElseThrow(() -> new RuntimeException("Meter not found: " + meterId));
    }

    public List<Meter> getMetersByCompanyId(Long companyId) {
        return meterRepository.findByCompanyIdOrderByMeterIdAsc(companyId);
    }

    public List<Meter> getMetersByLocationId(Long locationId) {
        return meterRepository.findByLocationIdOrderByMeterIdAsc(locationId);
    }

    public List<Meter> getMetersByCompanyAndLocation(Long companyId, Long locationId) {
        return meterRepository.findByCompanyIdAndLocationIdOrderByMeterIdAsc(companyId, locationId);
    }

    public Meter create(MeterRequest request) {
        if (meterRepository.existsById(request.getMeterId())) {
            throw new RuntimeException("Meter đã tồn tại với id: " + request.getMeterId());
        }

        Meter meter = new Meter();
        meter.setMeterId(request.getMeterId());
        meter.setCompanyId(request.getCompanyId());
        meter.setMeterName(request.getMeterName());
        meter.setLocationId(request.getLocationId());

        return meterRepository.save(meter);
    }

    public Meter update(String meterId, MeterRequest request) {
        Meter meter = meterRepository.findById(meterId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy meter với id: " + meterId));

        meter.setCompanyId(request.getCompanyId());
        meter.setMeterName(request.getMeterName());
        meter.setLocationId(request.getLocationId());

        return meterRepository.save(meter);
    }

    public void delete(String meterId) {
        Meter meter = meterRepository.findById(meterId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy meter với id: " + meterId));

        meterRepository.delete(meter);
    }
}