package com.icdc.insurance.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

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

        private double calculatePremium(PolicyProduct product, Vehicle vehicle) {
                double basePremium = product.getBasePremium();
                double idvFactor = vehicle.getIdv() * 0.01;
                return basePremium + idvFactor;
        }

        @Override
        public PurchaseResponse purchasePolicy(PurchaseRequest request) {
                boolean paymentSuccess = processPayment(request);
                if (!paymentSuccess) {
                        throw new RuntimeException("Payment failed. Policy cannot be generated.");
                }

                User user = userRepository.findById(request.getUserId())
                                .orElseThrow(() -> new RuntimeException("User not found"));
                PolicyProduct product = productRepository.findById(request.getProductId())
                                .orElseThrow(() -> new RuntimeException("Product not found"));
                Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

                double premium = calculatePremium(product, vehicle);
                LocalDate startDate = LocalDate.now();
                LocalDate endDate = startDate.plusDays(request.getDurationDays());

                String policyNo = "POL-" + UUID.randomUUID().toString();

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

        private boolean processPayment(PurchaseRequest request) {
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

        @Override
        public List<PolicyResponse> getPoliciesByUser(Long userId) {
                List<Policy> policies = policyRepository.findByUserId(userId);
                return policies.stream()
                                .map(PolicyResponse::fromEntity)
                                .collect(Collectors.toList());
        }

        @Override
        public QuoteResponse calculateQuote(QuoteRequest request) {
                // Find user and vehicle first to get their IDs
                User user = userRepository.findByFullName(request.getFullName())
                                .orElseThrow(() -> new RuntimeException(
                                                "User not found for full name: " + request.getFullName()));
                Vehicle vehicle = vehicleRepository.findByRegistrationNo(request.getVehicleNumber())
                                .orElseThrow(() -> new RuntimeException("Vehicle not found for registration number: "
                                                + request.getVehicleNumber()));

                // Assuming a default product for simplicity, or find based on some criteria
                PolicyProduct product = productRepository.findByName("Comprehensive Car")
                                .orElseThrow(() -> new RuntimeException("Product not found"));

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