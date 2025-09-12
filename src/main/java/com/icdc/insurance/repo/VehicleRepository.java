package com.icdc.insurance.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.icdc.insurance.model.Vehicle;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    // Add this new method to fix the compilation error
    Optional<Vehicle> findByRegistrationNo(String registrationNo);
}