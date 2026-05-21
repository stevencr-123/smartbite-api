package com.smartbite.operativo.dto.pago;

import com.smartbite.operativo.model.enums.EstadoPago;
import com.smartbite.operativo.model.enums.ProveedorPago;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoResponseDTO {

    private Long id;

    /*
     * =========================================================
     * INFORMACIÓN FINANCIERA
     * =========================================================
     */
    private BigDecimal monto;

    private LocalDateTime fechaPago;

    private EstadoPago estado;

    /*
     * =========================================================
     * REFERENCIA TRANSACCIONAL
     * =========================================================
     *
     * Ejemplos:
     * - Stripe PaymentIntent
     * - referencia bancaria
     * - comprobante
     */
    private String referenciaTransaccion;

    /*
     * =========================================================
     * GATEWAY / PROVEEDOR EXTERNO
     * =========================================================
     *
     * null si el pago fue interno/manual.
     */
    private ProveedorPago proveedorPago;

    /*
     * =========================================================
     * SESSION EXTERNA
     * =========================================================
     *
     * Útil para:
     * - frontend
     * - auditoría
     * - conciliación
     * - debugging
     */
    private String sessionId;

    /*
     * =========================================================
     * RELACIÓN CON ORDEN
     * =========================================================
     */
    private Long ordenId;

    /*
     * =========================================================
     * MÉTODO USADO POR EL CLIENTE
     * =========================================================
     *
     * Ejemplos:
     * - EFECTIVO
     * - TARJETA
     * - TRANSFERENCIA
     */
    private Long metodoPagoId;

    private String metodoPagoNombre;
}