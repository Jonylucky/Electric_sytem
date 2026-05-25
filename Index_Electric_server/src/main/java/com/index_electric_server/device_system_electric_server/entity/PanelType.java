package com.index_electric_server.device_system_electric_server.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "panel_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PanelType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "panel_type_id")
    private Long panelTypeId;

    @Column(name = "type_code", nullable = false, unique = true, length = 50)
    private String typeCode;

    @Column(name = "type_name", nullable = false, length = 255)
    private String typeName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
