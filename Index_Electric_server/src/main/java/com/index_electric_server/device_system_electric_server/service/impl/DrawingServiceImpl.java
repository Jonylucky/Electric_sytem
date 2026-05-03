package com.index_electric_server.device_system_electric_server.service.impl;

import com.index_electric_server.device_system_electric_server.dto.DrawingFileDto;
import com.index_electric_server.device_system_electric_server.dto.DrawingPanelCoordDto;
import com.index_electric_server.device_system_electric_server.entity.*;
import com.index_electric_server.device_system_electric_server.entity.DrawingFile;
import com.index_electric_server.device_system_electric_server.entity.DrawingPanelCoord;
import com.index_electric_server.device_system_electric_server.entity.ElectricalPanel;
import com.index_electric_server.device_system_electric_server.enums.DrawingType;
import com.index_electric_server.device_system_electric_server.enums.ExtractMethod;
import com.index_electric_server.device_system_electric_server.exception.ResourceNotFoundException;
import com.index_electric_server.device_system_electric_server.repository.*;
import com.index_electric_server.device_system_electric_server.repository.DrawingFileRepository;
import com.index_electric_server.device_system_electric_server.repository.DrawingPanelCoordRepository;
import com.index_electric_server.device_system_electric_server.repository.ElectricalPanelRepository;
import com.index_electric_server.device_system_electric_server.service.DrawingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DrawingServiceImpl implements DrawingService {

    private final DrawingFileRepository drawingFileRepository;
    private final DrawingPanelCoordRepository coordRepository;
    private final ElectricalPanelRepository panelRepository;

    @Override
    @Transactional
    public DrawingFileDto.Response saveDrawing(DrawingFileDto.Request req) {
        DrawingFile drawing = DrawingFile.builder()
            .objectType(req.getObjectType())
            .objectId(req.getObjectId())
            .drawingType(req.getDrawingType())
            .fileUrl(req.getFileUrl())
            .imageUrl(req.getImageUrl())
            .drawingCode(req.getDrawingCode())
            .drawingLabel(req.getDrawingLabel())
            .revision(req.getRevision())
            .issuedDate(req.getIssuedDate())
            .imageWPx(req.getImageWPx())
            .imageHPx(req.getImageHPx())
            .build();
        return toDrawingResponse(drawingFileRepository.save(drawing));
    }

    @Override
    public DrawingFileDto.Response findById(Long drawingId) {
        return toDrawingResponse(drawingFileRepository.findById(drawingId)
            .orElseThrow(() -> new ResourceNotFoundException("Drawing not found: " + drawingId)));
    }

    @Override
    public List<DrawingFileDto.Response> findByObject(String objectType, Long objectId) {
        return drawingFileRepository.findByObjectTypeAndObjectId(objectType, objectId)
            .stream().map(this::toDrawingResponse).collect(Collectors.toList());
    }

    @Override
    public List<DrawingFileDto.Response> findByObjectAndType(String objectType, Long objectId, DrawingType type) {
        return drawingFileRepository.findByObjectTypeAndObjectIdAndDrawingType(objectType, objectId, type)
            .stream().map(this::toDrawingResponse).collect(Collectors.toList());
    }

    @Override
    public DrawingFileDto.ViewerResponse loadViewerData(Long drawingId) {
        // 1 query: drawing + all coords + panels — no N+1
        DrawingFile drawing = drawingFileRepository.findWithPanelCoords(drawingId)
            .orElseThrow(() -> new ResourceNotFoundException("Drawing not found: " + drawingId));

        List<DrawingFileDto.ViewerResponse.PanelMarker> markers = drawing.getPanelCoords().stream()
            .map(coord -> {
                ElectricalPanel panel = coord.getPanel();
                return DrawingFileDto.ViewerResponse.PanelMarker.builder()
                    .panelId(panel.getPanelId())
                    .panelCode(panel.getPanelCode())
                    .panelName(panel.getPanelName())
                    .panelType(panel.getPanelType())
                    .floorLabel(panel.getFloorLabel())
                    .ratedCurrentA(panel.getRatedCurrentA())
                    .status(panel.getStatus())
                    .xPercent(coord.getXPercent())
                    .yPercent(coord.getYPercent())
                    .labelAnchor(coord.getLabelAnchor())
                    .extractMethod(coord.getExtractMethod() != null ? coord.getExtractMethod().name() : "manual")
                    .build();
            }).collect(Collectors.toList());

        return DrawingFileDto.ViewerResponse.builder()
            .drawingId(drawing.getDrawingId())
            .imageUrl(drawing.getImageUrl())
            .drawingLabel(drawing.getDrawingLabel())
            .drawingCode(drawing.getDrawingCode())
            .revision(drawing.getRevision())
            .imageWPx(drawing.getImageWPx())
            .imageHPx(drawing.getImageHPx())
            .panels(markers)
            .build();
    }

    @Override
    @Transactional
    public DrawingPanelCoordDto.Response upsertCoord(DrawingPanelCoordDto.Request req) {
        DrawingFile drawing = drawingFileRepository.findById(req.getDrawingId())
            .orElseThrow(() -> new ResourceNotFoundException("Drawing not found: " + req.getDrawingId()));
        ElectricalPanel panel = panelRepository.findById(req.getPanelId())
            .orElseThrow(() -> new ResourceNotFoundException("Panel not found: " + req.getPanelId()));

        DrawingPanelCoord coord = coordRepository
            .findByDrawingFile_DrawingIdAndPanel_PanelId(req.getDrawingId(), req.getPanelId())
            .orElse(DrawingPanelCoord.builder().drawingFile(drawing).panel(panel).build());

        coord.setXPercent(req.getXPercent());
        coord.setYPercent(req.getYPercent());
        if (req.getLabelAnchor() != null) coord.setLabelAnchor(req.getLabelAnchor());
        if (req.getExtractMethod() != null)
            coord.setExtractMethod(ExtractMethod.valueOf(req.getExtractMethod()));
        coord.setConfidence(req.getConfidence());

        return toCoordResponse(coordRepository.save(coord));
    }

    @Override
    @Transactional
    public List<DrawingPanelCoordDto.Response> batchUpsertCoords(List<DrawingPanelCoordDto.Request> requests) {
        return requests.stream().map(this::upsertCoord).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteDrawing(Long drawingId) {
        if (!drawingFileRepository.existsById(drawingId))
            throw new ResourceNotFoundException("Drawing not found: " + drawingId);
        drawingFileRepository.deleteById(drawingId);
    }

    // ── Mappers ──────────────────────────────────────────────

    private DrawingFileDto.Response toDrawingResponse(DrawingFile d) {
        return DrawingFileDto.Response.builder()
            .drawingId(d.getDrawingId())
            .objectType(d.getObjectType())
            .objectId(d.getObjectId())
            .drawingType(d.getDrawingType())
            .fileUrl(d.getFileUrl())
            .imageUrl(d.getImageUrl())
            .drawingCode(d.getDrawingCode())
            .drawingLabel(d.getDrawingLabel())
            .revision(d.getRevision())
            .issuedDate(d.getIssuedDate())
            .imageWPx(d.getImageWPx())
            .imageHPx(d.getImageHPx())
            .createdAt(d.getCreatedAt())
            .build();
    }

    private DrawingPanelCoordDto.Response toCoordResponse(DrawingPanelCoord c) {
        return DrawingPanelCoordDto.Response.builder()
            .coordId(c.getCoordId())
            .drawingId(c.getDrawingFile().getDrawingId())
            .panelId(c.getPanel().getPanelId())
            .panelCode(c.getPanel().getPanelCode())
            .panelName(c.getPanel().getPanelName())
            .xPercent(c.getXPercent())
            .yPercent(c.getYPercent())
            .labelAnchor(c.getLabelAnchor())
            .extractMethod(c.getExtractMethod() != null ? c.getExtractMethod().name() : "manual")
            .confidence(c.getConfidence())
            .updatedAt(c.getUpdatedAt())
            .build();
    }
}
