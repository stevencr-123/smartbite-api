package com.smartbite.operativo.dto.mesa;

import com.smartbite.operativo.model.enums.EstadoMesa;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActualizarEstadoMesaRequestDTO {

    @NotNull
    private EstadoMesa estado;
}