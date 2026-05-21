package com.smartbite.operativo.dto.cliente;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearClienteRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    private String nombre;

    @Size(max = 20)
    private String tipoDocumento;

    @Size(max = 30)
    private String numeroDocumento;

    @Email(message = "Email inválido")
    @Size(max = 100)
    private String email;

    @Size(max = 20)
    private String telefono;
}