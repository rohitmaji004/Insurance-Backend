package com.icdc.insurance.service;

import java.time.LocalDate;
import java.util.List;
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

        // Purchase policy using internal request
        @Override
        public PurchaseResponse purchasePolicy(PolicyRequest request) {
                User user = userRepository.findById(request.getUserId())
                                .orElseThrow(() -> new RuntimeException("User not found"));
                PolicyProduct product = productRepository.findById(request.getProductId())
                                .orElseThrow(() -> new RuntimeException("Product not found"));
                Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

                double premium = calculatePremium(product, vehicle);
                LocalDate startDate = LocalDate.now();
                LocalDate endDate = startDate.plusDays(request.getDurationDays());

                Policy policy = Policy.builder()
                                .user(user)
                                .product(product)
                                .vehicle(vehicle)
                                .policyNo("POL" + System.currentTimeMillis())
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

        // Purchase policy using external request (controller)
        @Override
        public PurchaseResponse purchasePolicy(PurchaseRequest request) {
                PolicyRequest policyRequest = new PolicyRequest();
                policyRequest.setUserId(request.getUserId());
                policyRequest.setProductId(request.getProductId());
                policyRequest.setVehicleId(request.getVehicleId());
                policyRequest.setDurationDays(request.getDurationDays());
                return purchasePolicy(policyRequest);
        }

        // Get policies by user
        @Override
        public List<PolicyResponse> getPoliciesByUser(Long userId) {
                List<Policy> policies = policyRepository.findByUserId(userId);
                return policies.stream()
                                .map(PolicyResponse::fromEntity)
                                .collect(Collectors.toList());
        }

        // Calculate quote using internal PolicyRequest
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

        // Calculate quote using external QuoteRequest
        @Override
        public QuoteResponse calculateQuote(QuoteRequest request) {
                PolicyRequest policyRequest = new PolicyRequest();
                policyRequest.setUserId(request.getUserId());
                policyRequest.setProductId(request.getProductId());
                policyRequest.setVehicleId(request.getVehicleId());
                policyRequest.setDurationDays(request.getDurationDays());
                return calculateQuote(policyRequest);
        }

        // Premium calculation logic
        private double calculatePremium(PolicyProduct product, Vehicle vehicle) {
                double basePremium = product.getBasePremium();
                double idvFactor = vehicle.getIdv() * 0.01;
                return basePremium + idvFactor;
        }
}
