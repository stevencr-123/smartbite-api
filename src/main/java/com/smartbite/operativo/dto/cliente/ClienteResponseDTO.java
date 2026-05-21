package com.smartbite.operativo.dto.cliente;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteResponseDTO {

    private Long id;

    private String nombre;

    private String tipoDocumento;

    private String numeroDocumento;

    private String email;

    private String telefono;

    private Boolean activo;
}