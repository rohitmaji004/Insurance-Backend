package com.icdc.insurance.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.icdc.insurance.dto.PolicyRequest;
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

/**
 * Service implementation for managing insurance policies.
 */
@Service
public class PolicyServiceImpl implements PolicyService {

        private final PolicyRepository policyRepository;
        private final UserRepository userRepository;
        private final PolicyProductRepository productRepository;
        private final VehicleRepository vehicleRepository;

        public PolicyServiceImpl(PolicyRepository policyRepository,
                        UserRepository userRepository,
                        PolicyProductRepository productRepository,
                        VehicleRepository vehicleRepository) {
                this.policyRepository = policyRepository;
                this.userRepository = userRepository;
                this.productRepository = productRepository;
                this.vehicleRepository = vehicleRepository;
        }

        // --- Core Logic for Premium Calculation and Policy Creation ---

        /**
         * Calculates the premium based on the product and vehicle.
         *
         * @param product The policy product.
         * @param vehicle The vehicle to be insured.
         * @return The calculated premium.
         */
        private double calculatePremium(PolicyProduct product, Vehicle vehicle) {
                double basePremium = product.getBasePremium();
                double idvFactor = vehicle.getIdv() * 0.01;
                return basePremium + idvFactor;
        }

        /**
         * Purchases a policy with the new PurchaseRequest DTO, including payment
         * processing.
         *
         * @param request The purchase request.
         * @return The response object with the generated policy details.
         */
        @Override
        public PurchaseResponse purchasePolicy(PurchaseRequest request) {
                // Step 1: Validate payment method and process payment (mocked)
                boolean paymentSuccess = processPayment(request);
                if (!paymentSuccess) {
                        throw new RuntimeException("Payment failed. Policy cannot be generated.");
                }

                // Step 2: Retrieve entities and perform basic validation
                User user = userRepository.findById(request.getUserId())
                                .orElseThrow(() -> new RuntimeException("User not found"));
                PolicyProduct product = productRepository.findById(request.getProductId())
                                .orElseThrow(() -> new RuntimeException("Product not found"));
                Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

                // Step 3: Calculate premium and dates
                double premium = calculatePremium(product, vehicle);
                LocalDate startDate = LocalDate.now();
                LocalDate endDate = startDate.plusDays(request.getDurationDays());

                // Step 4: Generate a unique, reliable policy number
                String policyNo = "POL-" + UUID.randomUUID().toString();

                // Step 5: Create and save the policy entity
                Policy policy = Policy.builder()
                                .user(user)
                                .product(product)
                                .vehicle(vehicle)
                                .policyNo(policyNo)
                                .status("ACTIVE")
                                .startDate(startDate)
                                .endDate(endDate)
                                .premium(premium)
                                .build();

                Policy saved = policyRepository.save(policy);

                // Step 6: Return the response DTO
                return PurchaseResponse.builder()
                                .policyId(saved.getId())
                                .policyNo(saved.getPolicyNo())
                                .status(saved.getStatus())
                                .startDate(saved.getStartDate().toString())
                                .endDate(saved.getEndDate().toString())
                                .premium(saved.getPremium())
                                .idv(vehicle.getIdv())
                                .build();
        }

        /**
         * Mock payment processing logic.
         *
         * @param request The purchase request with payment details.
         * @return true if payment is successful, false otherwise.
         */
        private boolean processPayment(PurchaseRequest request) {
                // In a real application, you would integrate with a payment gateway here.
                if ("CARD".equalsIgnoreCase(request.getPaymentMethod())) {
                        if (request.getCardNumber() == null || request.getCardNumber().isEmpty()) {
                                throw new IllegalArgumentException("Card number is required for card payment.");
                        }
                        if (request.getExpiryMonth() == null || request.getExpiryYear() == null) {
                                throw new IllegalArgumentException("Expiry date is required for card payment.");
                        }
                        System.out.println("Processing card payment for user " + request.getUserId());
                        return true;
                } else if ("UPI".equalsIgnoreCase(request.getPaymentMethod())) {
                        System.out.println("Processing UPI payment for user " + request.getUserId());
                        return true;
                } else {
                        throw new IllegalArgumentException("Invalid payment method.");
                }
        }

        // --- Other service methods ---

        @Override
        public List<PolicyResponse> getPoliciesByUser(Long userId) {
                List<Policy> policies = policyRepository.findByUserId(userId);
                return policies.stream()
                                .map(PolicyResponse::fromEntity)
                                .collect(Collectors.toList());
        }

        @Override
        public QuoteResponse calculateQuote(QuoteRequest request) {
                PolicyRequest policyRequest = new PolicyRequest();
                policyRequest.setUserId(request.getUserId());
                policyRequest.setProductId(request.getProductId());
                policyRequest.setVehicleId(request.getVehicleId());
                policyRequest.setDurationDays(request.getDurationDays());
                return calculateQuote(policyRequest);
        }

        @Override
        public QuoteResponse calculateQuote(PolicyRequest request) {
                PolicyProduct product = productRepository.findById(request.getProductId())
                                .orElseThrow(() -> new RuntimeException("Product not found"));
                Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

                double premium = calculatePremium(product, vehicle);
                LocalDate startDate = LocalDate.now();
                LocalDate endDate = startDate.plusDays(request.getDurationDays());

                return QuoteResponse.builder()
                                .productId(product.getId())
                                .vehicleId(vehicle.getId())
                                .premium(premium)
                                .idv(vehicle.getIdv())
                                .startDate(startDate)
                                .endDate(endDate)
                                .build();
        }
}
