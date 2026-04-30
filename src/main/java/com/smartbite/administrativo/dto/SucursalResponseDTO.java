package com.smartbite.administrativo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SucursalResponseDTO {
    private Long id;
    private String nombre;
    private String direccion;
    private String telefono;
    private String telefonoSecundario;
    private String email;
    private String horarioAtencion;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private Long restauranteId;
    private String restauranteNombre;
}