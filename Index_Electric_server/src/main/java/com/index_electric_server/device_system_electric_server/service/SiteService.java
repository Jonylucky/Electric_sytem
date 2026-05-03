package com.index_electric_server.device_system_electric_server.service;

import com.index_electric_server.device_system_electric_server.dto.SiteDto;
import java.util.List;

public interface SiteService {
    SiteDto.Response create(SiteDto.Request request);
    SiteDto.Response findById(Long id);
    List<SiteDto.Response> findAll();
    SiteDto.Response update(Long id, SiteDto.Request request);
    void delete(Long id);
}
