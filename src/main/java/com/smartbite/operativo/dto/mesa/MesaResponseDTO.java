package com.smartbite.operativo.dto.mesa;

import com.smartbite.operativo.model.enums.EstadoMesa;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MesaResponseDTO {

    private Long id;
    private Integer numero;
    private Integer capacidad;
    private EstadoMesa estado;
    private Boolean activa;
    private Long sucursalId;
}

