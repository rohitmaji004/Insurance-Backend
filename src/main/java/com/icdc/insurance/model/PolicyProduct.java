package com.icdc.insurance.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "policy_products")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PolicyProduct {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Double basePremium;
    private Integer tenureMonths;
}
