package com.smartbite.operativo.dto.qr;

import com.smartbite.operativo.model.enums.TipoQR;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerarQRRequestDTO {

    @NotNull
    private TipoQR tipo;

    private Long mesaId;
    private Long productoId;
}