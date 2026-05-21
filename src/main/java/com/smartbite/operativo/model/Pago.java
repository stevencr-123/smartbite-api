package com.smartbite.operativo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.smartbite.operativo.model.enums.EstadoPago;
import com.smartbite.operativo.model.enums.ProveedorPago;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "pagos",
        indexes = {
                @Index(name = "idx_pago_orden", columnList = "orden_id"),
                @Index(name = "idx_pago_referencia", columnList = "referencia_transaccion")
        }
)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoPago estado;

    /*
     * 🔥 ID externo de la transacción.
     *
     * Ejemplos:
     * - Stripe PaymentIntent ID
     * - UUID bancario
     * - referencia SPEI
     * - código transferencia
     */
    @Column(
            name = "referencia_transaccion",
            unique = true,
            length = 255
    )
    private String referenciaTransaccion;

    /*
     * 🔥 Plataforma que procesó el pago.
     *
     * NO representa el método usado por el cliente.
     *
     * Ejemplo:
     * método = TARJETA
     * proveedor = STRIPE
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "proveedor_pago", length = 50)
    private ProveedorPago proveedorPago;

    /*
     * 🔥 ID de sesión externa.
     *
     * Útil para:
     * - debugging
     * - reconciliación
     * - auditoría
     * - webhooks
     */
    @Column(name = "session_id", unique = true, length = 255)
    private String sessionId;

    /*
     * 🔥 Relación financiera principal.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_id", nullable = false)
    @JsonBackReference
    private Orden orden;

    /*
     * 🔥 Método usado por el cliente.
     *
     * Ejemplos:
     * - EFECTIVO
     * - TARJETA
     * - TRANSFERENCIA
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "metodo_pago_id", nullable = false)
    private MetodoPago metodoPago;
}