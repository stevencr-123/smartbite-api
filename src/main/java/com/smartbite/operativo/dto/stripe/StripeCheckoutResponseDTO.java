package com.smartbite.operativo.dto.stripe;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StripeCheckoutResponseDTO {

    private String sessionId;
    private String checkoutUrl;
}