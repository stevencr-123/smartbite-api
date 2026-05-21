package com.smartbite.administrativo.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompraRequestDTO {

    private LocalDateTime fechaCompra;

    @Size(max = 50, message = "El número de factura no puede exceder 50 caracteres")
    private String numeroFactura;

    @NotNull(message = "El ID del provedor es obligatorio")
    private Long provedorId;

    @NotNull(message = "El ID de la sucursal es obligatorio")
    private Long sucursalId;

    @NotEmpty(message = "La compra debe tener al menos un detalle")
    private List<DetalleCompraRequestDTO> detalles = new ArrayList<>();
}