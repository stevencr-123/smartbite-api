package com.smartbite.administrativo.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SucursalRequestDTO {

    @NotBlank(message = "El nombre de la sucursal es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    private String direccion;

    @Pattern(regexp = "^[0-9+\\-\\s]{7,20}$", message = "Formato de teléfono inválido")
    private String telefono;

    private String telefonoSecundario;

    @Email(message = "Debe ser un email válido")
    private String email;

    private String horarioAtencion;

    @NotNull(message = "El ID del restaurante es obligatorio")
    private Long restauranteId;
}