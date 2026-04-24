package com.smartbite.operativo.dto.venta;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VentaResponseDTO {

    private Long id;
    private LocalDateTime fechaVenta;
    private BigDecimal total;
    private Long ordenId;
}

