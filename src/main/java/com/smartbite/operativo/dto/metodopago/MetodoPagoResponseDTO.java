package com.smartbite.operativo.dto.metodopago;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetodoPagoResponseDTO {

    private Long id;
    private String nombre;
    private Boolean activo;
    private BigDecimal comision;
}

