package com.smartbite.operativo.dto.cliente;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteRequestDTO {

    @NotNull
    @Size(min = 1, max = 150)
    private String nombre;

    private String tipoDocumento;

    private String numeroDocumento;

    private String email;

    private String telefono;

    private Boolean activo;
}

