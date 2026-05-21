package com.smartbite.operativo.service;

import com.smartbite.operativo.dto.stripe.StripeCheckoutResponseDTO;

public interface StripeService {

    StripeCheckoutResponseDTO crearCheckoutSession(Long ordenId);
}