package com.smartbite.operativo.dto.orden;

import com.smartbite.operativo.dto.detalle.AgregarProductoRequestDTO;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearOrdenRequestDTO {

    private Long mesaId;
    private Long usuarioId;
    private Long sucursalId;
    private List<AgregarProductoRequestDTO> productos;
}

