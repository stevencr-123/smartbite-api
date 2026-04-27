package com.smartbite.administrativo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestauranteResponseDTO {
    private long id;
    private String nombre;
    private String nif;
    private String telefono;
    private String email;
    private String direccion;
    private String logoUrl;
    private boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

}
