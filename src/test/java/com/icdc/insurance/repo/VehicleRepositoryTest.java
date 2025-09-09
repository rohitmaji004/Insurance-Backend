package com.icdc.insurance.repo;

import com.icdc.insurance.model.User;
import com.icdc.insurance.model.Vehicle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class VehicleRepositoryTest {

    @Autowired private VehicleRepository vehicleRepository;
    @Autowired private UserRepository userRepository;

    @Test
    void testSaveAndFindVehicle() {
        User user = userRepository.save(
                User.builder().email("driver@test.com").password("pass").fullName("Driver User").build()
        );

        Vehicle vehicle = vehicleRepository.save(
                Vehicle.builder().registrationNo("KA01AB1234").model("Suzuki Swift").idv(300000.0).user(user).build()
        );

        Vehicle found = vehicleRepository.findById(vehicle.getId()).orElseThrow();
        assertThat(found.getRegistrationNo()).isEqualTo("KA01AB1234");
        assertThat(found.getUser().getFullName()).isEqualTo("Driver User");
    }
}
