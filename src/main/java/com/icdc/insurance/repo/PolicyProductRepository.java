package com.icdc.insurance.repo;

import com.icdc.insurance.model.PolicyProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PolicyProductRepository extends JpaRepository<PolicyProduct, Long> {
    Optional<PolicyProduct> findByName(String name);
}
