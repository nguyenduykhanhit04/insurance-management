package com.training.insurance.repository;

import com.training.insurance.entity.Insurance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InsuranceRepository extends JpaRepository<Insurance, Integer> {

    Optional<Insurance> findByInsuranceNumber(String insuranceNumber);

    boolean existsByInsuranceNumber(String insuranceNumber);

    boolean existsByInsuranceNumberAndInsuranceInternalIdNot(String insuranceNumber, Integer insuranceInternalId);
}
