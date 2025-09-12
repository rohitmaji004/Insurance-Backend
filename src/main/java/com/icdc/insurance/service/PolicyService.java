package com.icdc.insurance.service;

import java.util.List;

import com.icdc.insurance.dto.PolicyResponse;
import com.icdc.insurance.dto.PurchaseRequest;
import com.icdc.insurance.dto.PurchaseResponse;
import com.icdc.insurance.dto.QuoteRequest;
import com.icdc.insurance.dto.QuoteResponse;

/**
 * Service interface for managing insurance policies.
 */
public interface PolicyService {

    /**
     * Purchases a policy based on an external request from the controller.
     *
     * @param request The purchase request containing user, product, vehicle, and
     *                payment details.
     * @return The response object with the generated policy details.
     */
    PurchaseResponse purchasePolicy(PurchaseRequest request);

    /**
     * Retrieves all policies for a given user.
     *
     * @param userId The ID of the user.
     * @return A list of policy responses.
     */
    List<PolicyResponse> getPoliciesByUser(Long userId);

    /**
     * Calculates a quote based on an external request from the controller.
     *
     * @param request The quote request.
     * @return The quote response with calculated premium and other details.
     */
    QuoteResponse calculateQuote(QuoteRequest request);
}