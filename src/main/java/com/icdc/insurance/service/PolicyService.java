package com.icdc.insurance.service;

import java.util.List;

import com.icdc.insurance.dto.PolicyRequest;
import com.icdc.insurance.dto.PolicyResponse;
import com.icdc.insurance.dto.PurchaseRequest;
import com.icdc.insurance.dto.PurchaseResponse;
import com.icdc.insurance.dto.QuoteRequest;
import com.icdc.insurance.dto.QuoteResponse;

public interface PolicyService {

    // Purchase using internal request
    PurchaseResponse purchasePolicy(PolicyRequest request);

    // Purchase using external request (controller)
    PurchaseResponse purchasePolicy(PurchaseRequest request);

    // Fetch policies by user
    List<PolicyResponse> getPoliciesByUser(Long userId);

    // Calculate quote
    QuoteResponse calculateQuote(QuoteRequest request);

    QuoteResponse calculateQuote(PolicyRequest request);
}
