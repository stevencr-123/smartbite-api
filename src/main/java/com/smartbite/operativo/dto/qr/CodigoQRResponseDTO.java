package com.smartbite.operativo.dto.qr;

import com.smartbite.operativo.model.enums.TipoQR;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodigoQRResponseDTO {

    private Long id;
    private String contenido;
    private TipoQR tipo;
    private Boolean activo;
    private LocalDateTime fechaGeneracion;
    private Long mesaId;
    private Long productoId;
}
