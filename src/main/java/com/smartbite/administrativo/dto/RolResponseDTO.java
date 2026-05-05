package com.smartbite.administrativo.dto;

import com.smartbite.administrativo.enums.RolNombre;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolResponseDTO {
    private Long id;
    private RolNombre nombre;
    private String descripcion;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private Set<PermisoResponseDTO> permisos;
}