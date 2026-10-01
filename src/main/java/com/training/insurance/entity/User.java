package com.training.insurance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "tbl_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_internal_id")
    private Integer userInternalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_internal_id", nullable = false)
    private Company company;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insurance_internal_id", nullable = false)
    private Insurance insurance;

    @Column(name = "username", length = 15, nullable = false, unique = true)
    private String username;

    @Column(name = "password", length = 32, nullable = false)
    private String password;

    @Column(name = "user_full_name", length = 50, nullable = false)
    private String userFullName;

    @Column(name = "user_sex_division", length = 2, nullable = false)
    private String userSexDivision; // 01: Nam, 02: Nữ

    @Column(name = "birthdate")
    private LocalDate birthdate;

    public String getGenderText() {
        if ("01".equals(this.userSexDivision) || "1".equals(this.userSexDivision)) {
            return "Nam";
        } else if ("02".equals(this.userSexDivision) || "2".equals(this.userSexDivision)) {
            return "Nữ";
        }
        return "";
    }
}
