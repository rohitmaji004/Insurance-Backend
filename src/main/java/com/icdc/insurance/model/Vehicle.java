package com.icdc.insurance.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vehicles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Vehicle {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String registrationNo;
    private String model;
    private Double idv;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
