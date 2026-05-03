package com.index_electric_server.device_system_electric_server.controller;

import com.index_electric_server.device_system_electric_server.entity.AttachmentPhoto;
import com.index_electric_server.device_system_electric_server.enums.PhotoObjectType;
import com.index_electric_server.device_system_electric_server.repository.AttachmentPhotoRepository;
import com.index_electric_server.device_system_electric_server.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/photos")
@RequiredArgsConstructor
public class AttachmentPhotoController {

    private final AttachmentPhotoRepository photoRepository;

    /**
     * Get gallery for any object.
     * GET /api/photos?type=replacement&id=88
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AttachmentPhoto>>> findByObject(
            @RequestParam PhotoObjectType type,
            @RequestParam Long id) {
        return ResponseEntity.ok(ApiResponse.ok(
            photoRepository.findByObjectTypeAndObjectIdOrderBySortOrderAscTakenAtAsc(type, id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AttachmentPhoto>> create(@RequestBody AttachmentPhoto photo) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(photoRepository.save(photo)));
    }

    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<List<AttachmentPhoto>>> createBatch(
            @RequestBody List<AttachmentPhoto> photos) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(photoRepository.saveAll(photos)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        photoRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
