package com.smartbite.operativo.controller;

import com.smartbite.operativo.dto.stripe.StripeCheckoutRequestDTO;
import com.smartbite.operativo.dto.stripe.StripeCheckoutResponseDTO;
import com.smartbite.operativo.service.StripeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stripe")
@RequiredArgsConstructor
public class StripeController {

    private final StripeService stripeService;

    @PostMapping("/checkout")
    public ResponseEntity<StripeCheckoutResponseDTO> crearCheckout(
            @Valid @RequestBody StripeCheckoutRequestDTO request) {

        return ResponseEntity.ok(
                stripeService.crearCheckoutSession(
                        request.getOrdenId()
                )
        );
    }
}