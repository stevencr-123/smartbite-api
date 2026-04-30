package com.smartbite.administrativo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermisoRequestDTO {

    @NotBlank(message = "El nombre del permiso es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombre;

    @Size(max = 200, message = "La descripción no puede exceder 200 caracteres")
    private String descripcion;

    @Size(max = 100, message = "El recurso no puede exceder 100 caracteres")
    private String recurso;

    @Size(max = 20, message = "La acción no puede exceder 20 caracteres")
    private String accion;
}