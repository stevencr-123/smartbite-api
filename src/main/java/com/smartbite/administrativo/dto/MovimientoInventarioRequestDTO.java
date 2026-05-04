package com.smartbite.administrativo.dto;

import com.smartbite.administrativo.enums.TipoMovimientoInventario;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoInventarioRequestDTO {

    @NotNull(message = "El ID del ítem de inventario es obligatorio")
    private Long itemInventarioId;

    @NotNull(message = "El tipo de movimiento es obligatorio")
    private TipoMovimientoInventario tipo;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;

    @NotBlank(message = "El motivo es obligatorio")
    @Size(max = 255, message = "El motivo no puede exceder 255 caracteres")
    private String motivo;

    private String referencia;
    private Long referenciaId;
}