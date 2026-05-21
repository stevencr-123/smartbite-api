package com.smartbite.administrativo.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProvedorRequestDTO {

    @NotBlank(message = "El nombre del proveedor es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    @Size(max = 20, message = "El NIT no puede exceder 20 caracteres")
    private String nit;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String telefono;

    @Email(message = "Debe ser un email válido")
    private String email;

    @Size(max = 255, message = "La dirección no puede exceder 255 caracteres")
    private String direccion;

    @Size(max = 100, message = "El nombre de contacto no puede exceder 100 caracteres")
    private String contactoNombre;

    @Size(max = 20, message = "El teléfono de contacto no puede exceder 20 caracteres")
    private String contactoTelefono;

    private String observaciones;
}