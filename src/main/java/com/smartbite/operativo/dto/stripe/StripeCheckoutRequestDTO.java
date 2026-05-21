package com.smartbite.operativo.dto.stripe;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StripeCheckoutRequestDTO {

    @NotNull
    private Long ordenId;
}