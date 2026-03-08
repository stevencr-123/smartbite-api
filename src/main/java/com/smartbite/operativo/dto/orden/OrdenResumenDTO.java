package com.smartbite.operativo.dto.orden;

import com.smartbite.operativo.model.enums.EstadoOrden;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenResumenDTO {

    private Long id;
    private LocalDateTime fechaCreacion;
    private EstadoOrden estado;
    private BigDecimal total;
    private Long mesaId;
}
