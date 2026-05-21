package com.smartbite.operativo.dto.orden;

import com.smartbite.operativo.model.enums.EstadoOrden;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadoOrdenRequestDTO {

    @NotNull(message = "El estado es obligatorio")
    private EstadoOrden estado;
}