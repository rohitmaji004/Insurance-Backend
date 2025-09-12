package com.icdc.insurance.repo;

import com.icdc.insurance.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PolicyRepositoryTest {

    @Autowired private PolicyRepository policyRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private VehicleRepository vehicleRepository;
    @Autowired private PolicyProductRepository productRepository;

    private User user;
    private Vehicle vehicle;
    private PolicyProduct product;

    @BeforeEach
    void setUp() {
        user = userRepository.save(
                User.builder().email("test@test.com").password("pass").fullName("Test User").build()
        );

        vehicle = vehicleRepository.save(
                Vehicle.builder().registrationNo("AB1234").model("Honda City").idv(400000.0).user(user).build()
        );

        product = productRepository.save(
                PolicyProduct.builder().name("Car Comprehensive").basePremium(5000.0).tenureMonths(12).build()
        );

        Policy policy = Policy.builder()
                .policyNo("POL123")
                .user(user)
                .product(product)
                .vehicle(vehicle)
                .status("ACTIVE")
                .premium(5200.0)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(1))
                .build();

        policyRepository.save(policy);
    }

    @Test
    void testFindByUserId() {
        List<Policy> policies = policyRepository.findByUserId(user.getId());
        assertThat(policies).isNotEmpty();
        assertThat(policies.get(0).getPolicyNo()).isEqualTo("POL123");
    }

    @Test
    void testFindByUserIdAndStatus() {
        List<Policy> activePolicies = policyRepository.findByUserIdAndStatus(user.getId(), "ACTIVE");
        assertThat(activePolicies).hasSize(1);
    }

    @Test
    void testFindByPolicyNo() {
        Optional<Policy> policyOpt = policyRepository.findByPolicyNo("POL123");
        assertThat(policyOpt).isPresent();
        assertThat(policyOpt.get().getProduct().getName()).isEqualTo("Car Comprehensive");
    }
}
