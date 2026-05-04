package com.smartbite.administrativo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleCompraResponseDTO {
    private Long id;
    private Integer cantidad;
    private Double precioUnitario;
    private Double subtotal;
    private Integer cantidadRecibida;
    private LocalDateTime fechaCreacion;

    private Long itemInventarioId;
    private String itemInventarioNombre;
    private String itemInventarioUnidadMedida;
}