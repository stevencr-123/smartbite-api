package com.smartbite.operativo.dto.pago;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearPagoRequestDTO {

    @NotNull
    private Long ordenId;

    @NotNull
    private Long metodoPagoId;

    @NotNull
    @Positive
    @DecimalMin("0.01")
    private BigDecimal monto;

    @Size(max = 100)
    private String referenciaTransaccion;
}