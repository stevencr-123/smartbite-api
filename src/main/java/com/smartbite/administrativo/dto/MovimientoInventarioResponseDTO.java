package com.smartbite.administrativo.dto;

import com.smartbite.administrativo.enums.TipoMovimientoInventario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoInventarioResponseDTO {
    private Long id;
    private TipoMovimientoInventario tipo;
    private Integer cantidad;
    private String motivo;
    private String referencia;
    private Long referenciaId;
    private Integer stockAntes;
    private Integer stockDespues;
    private LocalDateTime fechaMovimiento;
    private LocalDateTime fechaCreacion;

    private Long itemInventarioId;
    private String itemInventarioNombre;
    private String itemInventarioUnidadMedida;
}