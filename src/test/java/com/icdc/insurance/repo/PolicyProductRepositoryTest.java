package com.icdc.insurance.repo;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.icdc.insurance.model.PolicyProduct;

@DataJpaTest
class PolicyProductRepositoryTest {

    @Autowired
    private PolicyProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll(); // clean previous data

        // Insert a test product
        PolicyProduct product = PolicyProduct.builder()
                .name("Bike Cover")
                .basePremium(1500.0)
                .tenureMonths(12)
                .build();
        productRepository.save(product);
    }

    @Test
    void testFindByName() {
        Optional<PolicyProduct> found = productRepository.findByName("Bike Cover");

        assertThat(found).isPresent(); // now it should pass
        assertThat(found.get().getBasePremium()).isEqualTo(1500.0);
    }
}
