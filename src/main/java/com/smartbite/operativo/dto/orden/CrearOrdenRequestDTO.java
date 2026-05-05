package com.smartbite.operativo.dto.orden;

import com.smartbite.operativo.dto.detalle.AgregarProductoRequestDTO;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearOrdenRequestDTO {

    @NotNull
    private Long mesaId;

    @NotNull
    private Long usuarioId;

    @NotNull
    private Long sucursalId;

    @NotEmpty
    private List<AgregarProductoRequestDTO> productos;
}