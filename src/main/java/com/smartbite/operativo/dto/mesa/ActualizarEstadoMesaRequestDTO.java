package com.smartbite.operativo.dto.mesa;

import com.smartbite.operativo.model.enums.EstadoMesa;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActualizarEstadoMesaRequestDTO {

    private EstadoMesa estado;
}

