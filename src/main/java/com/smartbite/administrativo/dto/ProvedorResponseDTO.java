package com.smartbite.administrativo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProvedorResponseDTO {
    private Long id;
    private String nombre;
    private String nit;
    private String telefono;
    private String email;
    private String direccion;
    private String contactoNombre;
    private String contactoTelefono;
    private String observaciones;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}