package com.smartbite.operativo.dto.mesa;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearMesaRequestDTO {

    @NotNull(message = "El número de mesa es obligatorio")
    @Min(value = 1, message = "El número debe ser mayor a 0")
    private Integer numero;

    @NotNull(message = "La capacidad es obligatoria")
    @Min(value = 1, message = "La capacidad debe ser mayor a 0")
    private Integer capacidad;

    @NotNull(message = "La sucursal es obligatoria")
    private Long sucursalId;
}