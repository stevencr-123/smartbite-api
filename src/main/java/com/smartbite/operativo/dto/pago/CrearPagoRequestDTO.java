package com.smartbite.operativo.dto.pago;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearPagoRequestDTO {

    private Long ordenId;
    private Long metodoPagoId;
    private BigDecimal monto;
    private String referenciaTransaccion;
}

