package com.smartbite.administrativo.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoIngredienteRequestDTO {
    @NotNull(message = "El ID del producto es obligatorio")
    private Long productoId;

    @NotNull(message = "El ID del ítem de inventario es obligatorio")
    private Long itemInventarioId;

    @NotNull(message = "La cantidad requerida es obligatoria")
    @DecimalMin(value = "0.0", inclusive = false, message = "La cantidad debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "La cantidad debe tener máximo 2 decimales")
    private Double cantidadRequerida;

    @Size(max = 50, message = "La unidad de medida no puede exceder 50 caracteres")
    private String unidadMedida;
}
