package com.smartbite.operativo.dto.factura;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacturaResponseDTO {

    private Long id;
    private String numero;
    private LocalDateTime fechaEmision;
    private BigDecimal subtotal;
    private BigDecimal impuestos;
    private BigDecimal total;
    private Long ventaId;
    private Long clienteId;
}

