package com.smartbite.operativo.dto.orden;

import com.smartbite.operativo.dto.detalle.DetalleOrdenResponseDTO;
import com.smartbite.operativo.dto.pago.PagoResponseDTO;
import com.smartbite.operativo.model.enums.EstadoOrden;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenResponseDTO {

    private Long id;
    private LocalDateTime fechaCreacion;
    private EstadoOrden estado;
    private BigDecimal total;

    private Long mesaId;
    private Integer numeroMesa; // 🔥 agregado

    private Long usuarioId;
    private String nombreUsuario; // 🔥 agregado (útil para UI)

    private Long sucursalId;

    @Builder.Default
    private List<DetalleOrdenResponseDTO> detalles = new ArrayList<>();

    @Builder.Default
    private List<PagoResponseDTO> pagos = new ArrayList<>();
}