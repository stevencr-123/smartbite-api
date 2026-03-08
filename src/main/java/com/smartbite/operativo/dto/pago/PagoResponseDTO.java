package com.smartbite.operativo.dto.pago;

import com.smartbite.operativo.model.enums.EstadoPago;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoResponseDTO {

    private Long id;
    private BigDecimal monto;
    private LocalDateTime fechaPago;
    private EstadoPago estado;
    private String referenciaTransaccion;
    private Long ordenId;
    private Long metodoPagoId;
    private String metodoPagoNombre;
}

