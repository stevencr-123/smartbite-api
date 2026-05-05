package com.smartbite.administrativo.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemInventarioRequestDTO {

    @NotBlank(message = "El nombre del ítem es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    private String descripcion;

    @NotNull(message = "El stock actual es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stockActual;

    @NotNull(message = "El stock mínimo es obligatorio")
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    private Integer stockMinimo;

    @NotBlank(message = "La unidad de medida es obligatoria")
    @Size(max = 20, message = "La unidad de medida no puede exceder 20 caracteres")
    private String unidadMedida;  // kg, g, unidad, L, ml

    @DecimalMin(value = "0.0", inclusive = true, message = "El costo unitario no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "El costo debe tener máximo 2 decimales")
    private Double costoUnitario;

    private String ubicacion;  // Ubicación en almacén (opcional)

    @NotNull(message = "El ID de la sucursal es obligatorio")
    private Long sucursalId;
}