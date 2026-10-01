package com.training.insurance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tbl_company")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_internal_id")
    private Integer companyInternalId;

    @Column(name = "company_name", length = 50, nullable = false)
    private String companyName;

    @Column(name = "address", length = 100, nullable = false)
    private String address;

    @Column(name = "email", length = 50)
    private String email;

    @Column(name = "telephone", length = 15)
    private String telephone;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<User> users = new ArrayList<>();
}
