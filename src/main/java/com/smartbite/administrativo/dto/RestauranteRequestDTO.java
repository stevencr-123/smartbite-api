package com.smartbite.administrativo.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestauranteRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombre;

    @NotBlank(message = "El NIF es obligatorio")
    @Pattern(regexp = "^\\d{1,10}-\\d{1}$", message = "Formato de NIT inválido. Ejemplo: 123456789-0")
    private String nif;

    @Pattern(regexp = "^[0-9+\\-\\s]{7,20}$", message = "Formato de teléfono inválido")
    private String telefono;

    private String telefonoSecundario;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ser un email válido")
    private String email;

    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    private String logoUrl;
}