package com.icdc.insurance.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.icdc.insurance.model.Policy;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {
    List<Policy> findByUserId(Long userId);

    List<Policy> findByUserIdAndStatus(Long userId, String status);

    Optional<Policy> findByPolicyNo(String policyNo);
}
