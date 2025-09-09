package com.icdc.insurance.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.icdc.insurance.dto.PolicyRequest;
import com.icdc.insurance.dto.PolicyResponse;
import com.icdc.insurance.dto.PurchaseRequest;
import com.icdc.insurance.dto.PurchaseResponse;
import com.icdc.insurance.dto.QuoteResponse;
import com.icdc.insurance.model.Policy;
import com.icdc.insurance.model.PolicyProduct;
import com.icdc.insurance.model.User;
import com.icdc.insurance.model.Vehicle;
import com.icdc.insurance.repo.PolicyProductRepository;
import com.icdc.insurance.repo.PolicyRepository;
import com.icdc.insurance.repo.UserRepository;
import com.icdc.insurance.repo.VehicleRepository;

class PolicyServiceImplTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PolicyProductRepository productRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private PolicyServiceImpl policyService;

    private User user;
    private PolicyProduct product;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = User.builder()
                .id(1L)
                .fullName("Test User")
                .email("test@test.com")
                .build();

        product = PolicyProduct.builder()
                .id(1L)
                .name("Car Comprehensive")
                .basePremium(5000.0)
                .tenureMonths(12)
                .build();

        vehicle = Vehicle.builder()
                .id(1L)
                .registrationNo("KA01AB1234")
                .model("Honda City")
                .idv(400000.0)
                .user(user)
                .build();
    }

    @Test
    void testPurchasePolicy_Success() {
        PolicyRequest request = new PolicyRequest();
        request.setUserId(1L);
        request.setProductId(1L);
        request.setVehicleId(1L);
        request.setDurationDays(365);

        Policy savedPolicy = Policy.builder()
                .id(1001L)
                .policyNo("POL123")
                .user(user)
                .product(product)
                .vehicle(vehicle)
                .premium(5400.0)
                .status("ACTIVE")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(365))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(policyRepository.save(any(Policy.class))).thenReturn(savedPolicy);

        PurchaseResponse response = policyService.purchasePolicy(request);

        assertThat(response).isNotNull();
        assertThat(response.getPolicyNo()).isEqualTo("POL123");
        assertThat(response.getPremium()).isGreaterThan(5000.0);

        verify(policyRepository, times(1)).save(any(Policy.class));
    }

    @Test
    void testPurchasePolicy_UserNotFound() {
        PolicyRequest request = new PolicyRequest();
        request.setUserId(99L);
        request.setProductId(1L);
        request.setVehicleId(1L);
        request.setDurationDays(365);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> policyService.purchasePolicy(request));
    }

    @Test
    void testPurchasePolicy_ProductNotFound() {
        PolicyRequest request = new PolicyRequest();
        request.setUserId(1L);
        request.setProductId(99L);
        request.setVehicleId(1L);
        request.setDurationDays(365);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> policyService.purchasePolicy(request));
    }

    @Test
    void testPurchasePolicy_VehicleNotFound() {
        PolicyRequest request = new PolicyRequest();
        request.setUserId(1L);
        request.setProductId(1L);
        request.setVehicleId(99L);
        request.setDurationDays(365);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> policyService.purchasePolicy(request));
    }

    @Test
    void testCalculateQuote_Success() {
        PolicyRequest request = new PolicyRequest();
        request.setProductId(1L);
        request.setVehicleId(1L);
        request.setDurationDays(365);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        QuoteResponse response = policyService.calculateQuote(request);

        assertThat(response).isNotNull();
        assertThat(response.getPremium()).isGreaterThan(5000.0);
        assertThat(response.getIdv()).isEqualTo(vehicle.getIdv());
    }

    @Test
    void testGetPoliciesByUser() {
        Policy policy1 = Policy.builder()
                .id(1001L)
                .policyNo("POL111")
                .user(user)
                .product(product)
                .vehicle(vehicle)
                .premium(5200.0)
                .status("ACTIVE")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(365))
                .build();

        Policy policy2 = Policy.builder()
                .id(1002L)
                .policyNo("POL222")
                .user(user)
                .product(product)
                .vehicle(vehicle)
                .premium(6000.0)
                .status("ACTIVE")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(365))
                .build();

        when(policyRepository.findByUserId(1L)).thenReturn(Arrays.asList(policy1, policy2));

        List<PolicyResponse> responses = policyService.getPoliciesByUser(1L);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getPolicyNo()).isEqualTo("POL111");
    }

    @Test
    void testPurchasePolicy_WithPurchaseRequest() {
        PurchaseRequest request = new PurchaseRequest();
        request.setUserId(1L);
        request.setProductId(1L);
        request.setVehicleId(1L);
        request.setDurationDays(365);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        Policy savedPolicy = Policy.builder()
                .id(2001L)
                .policyNo("POL567")
                .user(user)
                .product(product)
                .vehicle(vehicle)
                .premium(5600.0)
                .status("ACTIVE")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(365))
                .build();

        when(policyRepository.save(any(Policy.class))).thenReturn(savedPolicy);

        PurchaseResponse response = policyService.purchasePolicy(request);

        assertThat(response).isNotNull();
        assertThat(response.getPolicyId()).isEqualTo(2001L);
        assertThat(response.getPolicyNo()).isEqualTo("POL567");
    }
}
