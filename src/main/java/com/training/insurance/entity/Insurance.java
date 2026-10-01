package com.training.insurance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "tbl_insurance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Insurance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "insurance_internal_id")
    private Integer insuranceInternalId;

    @Column(name = "insurance_number", length = 10, nullable = false, unique = true)
    private String insuranceNumber;

    @Column(name = "insurance_start_date", nullable = false)
    private LocalDate insuranceStartDate;

    @Column(name = "insurance_end_date", nullable = false)
    private LocalDate insuranceEndDate;

    @Column(name = "place_of_register", length = 50, nullable = false)
    private String placeOfRegister;

    @OneToOne(mappedBy = "insurance", cascade = CascadeType.ALL, orphanRemoval = true)
    private User user;
}
