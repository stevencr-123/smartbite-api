package com.smartbite.administrativo.dto;

import com.smartbite.administrativo.enums.EstadoCompra;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompraResponseDTO {
    private Long id;
    private LocalDateTime fechaCompra;
    private String numeroFactura;
    private Double total;
    private EstadoCompra estado;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    private Long provedorId;
    private String provedorNombre;
    private String provedorNit;

    private Long sucursalId;
    private String sucursalNombre;

    private List<DetalleCompraResponseDTO> detalles;
}