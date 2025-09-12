package com.icdc.insurance.service;

import static org.assertj.core.api.Assertions.*;
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

import com.icdc.insurance.dto.PolicyResponse;
import com.icdc.insurance.dto.PurchaseRequest;
import com.icdc.insurance.dto.PurchaseResponse;
import com.icdc.insurance.dto.QuoteRequest;
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
                .name("Comprehensive Car")
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

        // ✅ Default stubbing (valid case)
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
    }

    @Test
    void testPurchasePolicy_Success() {
        PurchaseRequest request = new PurchaseRequest(
                1L, 1L, 1L, 365,
                "CARD", "1234123412341234", "John Doe", 12, 2030, "123");

        Policy savedPolicy = Policy.builder()
                .id(1001L)
                .policyNo("POL-test-uuid")
                .user(user)
                .product(product)
                .vehicle(vehicle)
                .premium(5400.0)
                .status("ACTIVE")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(365))
                .build();

        when(policyRepository.save(any(Policy.class))).thenReturn(savedPolicy);

        PurchaseResponse response = policyService.purchasePolicy(request);

        assertThat(response).isNotNull();
        assertThat(response.getPolicyNo()).startsWith("POL-");
        assertThat(response.getPremium()).isGreaterThan(5000.0);

        verify(policyRepository, times(1)).save(any(Policy.class));
    }

    @Test
    void testPurchasePolicy_InvalidPaymentMethod() {
        PurchaseRequest request = new PurchaseRequest(
                1L, 1L, 1L, 365,
                "PAYLATER", null, null, null, null, null);

        assertThatThrownBy(() -> policyService.purchasePolicy(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid payment method");
    }

    @Test
    void testPurchasePolicy_MissingCardDetails() {
        PurchaseRequest request = new PurchaseRequest(
                1L, 1L, 1L, 365,
                "CARD", null, "John Doe", 12, 2030, "123");

        assertThatThrownBy(() -> policyService.purchasePolicy(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Card number is required");
    }

    @Test
    void testPurchasePolicy_UserNotFound() {
        PurchaseRequest request = new PurchaseRequest(
                99L, 1L, 1L, 365,
                "UPI", null, null, null, null, null);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> policyService.purchasePolicy(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void testPurchasePolicy_ProductNotFound() {
        PurchaseRequest request = new PurchaseRequest(
                1L, 99L, 1L, 365,
                "UPI", null, null, null, null, null);

        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> policyService.purchasePolicy(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    void testPurchasePolicy_VehicleNotFound() {
        PurchaseRequest request = new PurchaseRequest(
                1L, 1L, 99L, 365,
                "UPI", null, null, null, null, null);

        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> policyService.purchasePolicy(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Vehicle not found");
    }

    @Test
    void testCalculateQuote_Success() {
        QuoteRequest request = new QuoteRequest();
        request.setFullName("Test User");
        request.setVehicleNumber("KA01AB1234");
        request.setDurationDays(365);

        when(userRepository.findByFullName(anyString())).thenReturn(Optional.of(user));
        when(vehicleRepository.findByRegistrationNo(anyString())).thenReturn(Optional.of(vehicle));
        when(productRepository.findByName(anyString())).thenReturn(Optional.of(product));

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
}
