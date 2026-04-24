package com.smartbite.operativo.dto.detalle;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgregarProductoRequestDTO {

    @NotNull
    private Long productoId;

    @NotNull
    @Positive
    private Integer cantidad;
}
