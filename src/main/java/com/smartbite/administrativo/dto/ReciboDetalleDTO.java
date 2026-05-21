package com.smartbite.administrativo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReciboDetalleDTO {

    @NotNull(message = "El ID del detalle de compra es obligatorio")
    private Long detalleCompraId;

    @NotNull(message = "La cantidad recibida es obligatoria")
    @Positive(message = "La cantidad recibida debe ser mayor a 0")
    private Integer cantidadRecibida;
}