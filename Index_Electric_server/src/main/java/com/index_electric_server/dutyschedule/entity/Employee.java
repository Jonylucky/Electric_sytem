package com.index_electric_server.dutyschedule.entity;
import jakarta.persistence.*;
import  lombok.*;

@Entity
@Table(name="employee")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    private boolean active = true;


    @Column(name = "rotation_order", nullable = false)
    private Integer rotationOrder; // Thứ tự xoay vòng trực: 1, 2, 3, 4...

}
