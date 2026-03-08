package com.smartbite.operativo.dto.detalle;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgregarProductoRequestDTO {

    private Long productoId;
    private Integer cantidad;
}

