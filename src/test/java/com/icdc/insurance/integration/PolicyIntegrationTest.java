package com.icdc.insurance.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icdc.insurance.dto.PurchaseRequest;
import com.icdc.insurance.dto.QuoteRequest;
import com.icdc.insurance.model.PolicyProduct;
import com.icdc.insurance.model.User;
import com.icdc.insurance.model.Vehicle;
import com.icdc.insurance.repo.PolicyProductRepository;
import com.icdc.insurance.repo.PolicyRepository;
import com.icdc.insurance.repo.UserRepository;
import com.icdc.insurance.repo.VehicleRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // ensures rollback after each test
class PolicyIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private VehicleRepository vehicleRepository;

        @Autowired
        private PolicyProductRepository productRepository;

        @Autowired
        private PolicyRepository policyRepository;

        private Long userId;
        private Long productId;
        private Long vehicleId;

        @BeforeEach
        void setUp() {
                // Delete child entities first to avoid FK constraint issues
                policyRepository.deleteAll();
                vehicleRepository.deleteAll();
                userRepository.deleteAll();
                productRepository.deleteAll();

                User user = userRepository.save(User.builder()
                                .email("test@test.com")
                                .password("pass")
                                .fullName("Test User")
                                .build());
                userId = user.getId();

                PolicyProduct product = productRepository.save(PolicyProduct.builder()
                                .name("Car Comprehensive")
                                .basePremium(5000.0)
                                .tenureMonths(12)
                                .build());
                productId = product.getId();

                Vehicle vehicle = vehicleRepository.save(Vehicle.builder()
                                .registrationNo("KA01AB1234")
                                .model("Honda City")
                                .idv(400000.0)
                                .user(user)
                                .build());
                vehicleId = vehicle.getId();
        }

        @Test
        void testPurchasePolicyEndpoint() throws Exception {
                // Updated payload to include card payment details
                PurchaseRequest request = new PurchaseRequest(
                                userId,
                                productId,
                                vehicleId,
                                365,
                                "CARD", // Change to CARD
                                "4242424242424242", // A valid test credit card number
                                "John Doe",
                                12,
                                2030,
                                "123");

                mockMvc.perform(post("/api/policies/purchase")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.policyId").exists())
                                .andExpect(jsonPath("$.policyNo").exists())
                                .andExpect(jsonPath("$.status").value("ACTIVE"));
        }

        @Test
        void testQuoteEndpoint() throws Exception {
                QuoteRequest request = new QuoteRequest();
                request.setUserId(userId);
                request.setProductId(productId);
                request.setVehicleId(vehicleId);
                request.setDurationDays(365);

                mockMvc.perform(post("/api/policies/quote")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.premium").exists())
                                .andExpect(jsonPath("$.idv").value(400000.0));
        }

        @Test
        void testGetPoliciesByUserEndpoint() throws Exception {
                PurchaseRequest request = new PurchaseRequest(
                                userId,
                                productId,
                                vehicleId,
                                365,
                                "UPI",
                                null,
                                null,
                                null,
                                null,
                                null);

                // First, purchase a policy
                mockMvc.perform(post("/api/policies/purchase")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk());

                // Then, fetch policies by user
                mockMvc.perform(get("/api/policies/user/" + userId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].policyNo").exists())
                                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
        }
}
