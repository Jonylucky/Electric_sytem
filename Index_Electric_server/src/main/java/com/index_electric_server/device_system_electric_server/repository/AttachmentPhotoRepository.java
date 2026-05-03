package com.index_electric_server.device_system_electric_server.repository;

import com.index_electric_server.device_system_electric_server.entity.AttachmentPhoto;
import com.index_electric_server.device_system_electric_server.enums.PhotoObjectType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentPhotoRepository extends JpaRepository<AttachmentPhoto, Long> {
    // composite index: object_type + object_id + sort_order
    List<AttachmentPhoto> findByObjectTypeAndObjectIdOrderBySortOrderAscTakenAtAsc(
        PhotoObjectType objectType, Long objectId);
}
