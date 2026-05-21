package com.smartbite.administrativo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoIngredienteResponseDTO {
    private Long id;
    private Double cantidadRequerida;
    private String unidadMedida;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;


    private Long productoId;
    private String productoNombre;
    private Double productoPrecio;


    private Long itemInventarioId;
    private String itemInventarioNombre;
    private Integer itemInventarioStockActual;
    private String itemInventarioUnidadMedida;
}