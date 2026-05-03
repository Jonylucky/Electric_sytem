package com.index_electric_server.device_system_electric_server.service.impl;

import com.index_electric_server.device_system_electric_server.dto.SiteDto;
import com.index_electric_server.device_system_electric_server.entity.Site;
import com.index_electric_server.device_system_electric_server.exception.DuplicateCodeException;
import com.index_electric_server.device_system_electric_server.exception.ResourceNotFoundException;
import com.index_electric_server.device_system_electric_server.repository.SiteRepository;
import com.index_electric_server.device_system_electric_server.service.SiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SiteServiceImpl implements SiteService {

    private final SiteRepository siteRepository;

    @Override
    @Transactional
    public SiteDto.Response create(SiteDto.Request request) {
        if (siteRepository.existsBySiteCode(request.getSiteCode())) {
            throw new DuplicateCodeException("Site code already exists: " + request.getSiteCode());
        }
        Site site = Site.builder()
            .siteCode(request.getSiteCode())
            .siteName(request.getSiteName())
            .address(request.getAddress())
            .build();
        return toResponse(siteRepository.save(site));
    }

    @Override
    public SiteDto.Response findById(Long id) {
        return toResponse(siteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Site not found: " + id)));
    }

    @Override
    public List<SiteDto.Response> findAll() {
        return siteRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SiteDto.Response update(Long id, SiteDto.Request request) {
        Site site = siteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Site not found: " + id));
        site.setSiteName(request.getSiteName());
        site.setAddress(request.getAddress());
        return toResponse(siteRepository.save(site));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!siteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Site not found: " + id);
        }
        siteRepository.deleteById(id);
    }

    private SiteDto.Response toResponse(Site s) {
        return SiteDto.Response.builder()
            .siteId(s.getSiteId())
            .siteCode(s.getSiteCode())
            .siteName(s.getSiteName())
            .address(s.getAddress())
            .createdAt(s.getCreatedAt())
            .build();
    }
}
