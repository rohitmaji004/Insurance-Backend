package com.icdc.insurance.controller;

import com.icdc.insurance.dto.PolicyResponse;
import com.icdc.insurance.dto.PurchaseRequest;
import com.icdc.insurance.dto.PurchaseResponse;
import com.icdc.insurance.dto.QuoteRequest;
import com.icdc.insurance.dto.QuoteResponse;
import com.icdc.insurance.service.PolicyService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/policies")
public class PolicyController {
    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @PostMapping("/purchase")
    public ResponseEntity<PurchaseResponse> purchasePolicy(@Valid @RequestBody PurchaseRequest request) {
        return ResponseEntity.ok(policyService.purchasePolicy(request));
    }

    @PostMapping("/quote")
    public ResponseEntity<QuoteResponse> getQuote(@Valid @RequestBody QuoteRequest request) {
        return ResponseEntity.ok(policyService.calculateQuote(request));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PolicyResponse>> getPoliciesByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(policyService.getPoliciesByUser(userId));
    }
}
