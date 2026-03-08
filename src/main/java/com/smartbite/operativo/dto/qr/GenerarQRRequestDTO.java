package com.smartbite.operativo.dto.qr;

import com.smartbite.operativo.model.enums.TipoQR;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerarQRRequestDTO {

    private TipoQR tipo;
    private Long mesaId;
    private Long productoId;
}

