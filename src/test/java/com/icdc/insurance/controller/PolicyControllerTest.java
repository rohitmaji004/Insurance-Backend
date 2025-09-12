package com.icdc.insurance.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icdc.insurance.dto.PolicyResponse;
import com.icdc.insurance.dto.PurchaseRequest;
import com.icdc.insurance.dto.PurchaseResponse;
import com.icdc.insurance.dto.QuoteRequest;
import com.icdc.insurance.dto.QuoteResponse;
import com.icdc.insurance.service.PolicyService;

@WebMvcTest(PolicyController.class)
class PolicyControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private PolicyService policyService;

        @Test
        void testPurchasePolicy_Success() throws Exception {
                PurchaseRequest request = new PurchaseRequest(
                                1L,
                                1L,
                                1L,
                                365,
                                "CARD",
                                "4242424242424242",
                                "John Doe",
                                12,
                                2030,
                                "123");

                PurchaseResponse response = PurchaseResponse.builder()
                                .policyId(1001L)
                                .policyNo("POL123456")
                                .status("ACTIVE")
                                .startDate("2025-09-10")
                                .endDate("2026-09-10")
                                .premium(5200.0)
                                .idv(450000.0)
                                .build();

                when(policyService.purchasePolicy(any(PurchaseRequest.class))).thenReturn(response);

                mockMvc.perform(post("/api/policies/purchase")
                                .with(user("testUser").roles("USER"))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.policyId").value(1001L))
                                .andExpect(jsonPath("$.policyNo").value("POL123456"))
                                .andExpect(jsonPath("$.status").value("ACTIVE"));
        }

        @Test
        void testPurchasePolicy_UserNotFound() throws Exception {
                PurchaseRequest request = new PurchaseRequest(
                                99L,
                                1L,
                                1L,
                                365,
                                "UPI",
                                null,
                                null,
                                null,
                                null,
                                null);

                when(policyService.purchasePolicy(any(PurchaseRequest.class)))
                                .thenThrow(new RuntimeException("User not found"));

                mockMvc.perform(post("/api/policies/purchase")
                                .with(user("testUser").roles("USER"))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void testGetQuote() throws Exception {
                QuoteRequest request = new QuoteRequest();
                request.setUserId(1L);
                request.setProductId(1L);
                request.setVehicleId(1L);
                request.setDurationDays(365);

                QuoteResponse response = QuoteResponse.builder()
                                .premium(5200.0)
                                .idv(450000.0)
                                .startDate(LocalDate.of(2025, 9, 10))
                                .endDate(LocalDate.of(2026, 9, 10))
                                .build();

                when(policyService.calculateQuote(any(QuoteRequest.class))).thenReturn(response);

                mockMvc.perform(post("/api/policies/quote")
                                .with(user("testUser").roles("USER"))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.premium").value(5200.0))
                                .andExpect(jsonPath("$.idv").value(450000.0));
        }

        @Test
        void testGetPoliciesByUser() throws Exception {
                PolicyResponse policy1 = PolicyResponse.builder()
                                .policyId(1001L)
                                .policyNo("POL111")
                                .productName("Car Comprehensive")
                                .startDate(LocalDate.of(2025, 1, 1))
                                .endDate(LocalDate.of(2026, 1, 1))
                                .premium(5000.0)
                                .idv(400000.0)
                                .status("ACTIVE")
                                .build();

                PolicyResponse policy2 = PolicyResponse.builder()
                                .policyId(1002L)
                                .policyNo("POL222")
                                .productName("Bike Cover")
                                .startDate(LocalDate.of(2025, 3, 1))
                                .endDate(LocalDate.of(2026, 3, 1))
                                .premium(1500.0)
                                .idv(60000.0)
                                .status("ACTIVE")
                                .build();

                List<PolicyResponse> responses = Arrays.asList(policy1, policy2);
                when(policyService.getPoliciesByUser(1L)).thenReturn(responses);

                mockMvc.perform(get("/api/policies/user/1")
                                .with(user("testUser").roles("USER")))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].policyNo").value("POL111"))
                                .andExpect(jsonPath("$[1].policyNo").value("POL222"))
                                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
        }
}
