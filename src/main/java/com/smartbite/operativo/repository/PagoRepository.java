package com.smartbite.operativo.repository;

import com.smartbite.operativo.model.Pago;
import com.smartbite.operativo.model.enums.EstadoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    /*
     * =========================================================
     * CONSULTAS POR ORDEN
     * =========================================================
     */
    List<Pago> findByOrdenId(Long ordenId);

    List<Pago> findByOrdenIdAndEstado(
            Long ordenId,
            EstadoPago estado
    );

    Optional<Pago> findFirstByOrdenIdOrderByIdDesc(
            Long ordenId
    );

    Optional<Pago> findFirstByOrdenIdAndEstadoOrderByIdDesc(
            Long ordenId,
            EstadoPago estado
    );

    /*
     * =========================================================
     * REFERENCIA TRANSACCIONAL
     * =========================================================
     *
     * Ejemplo:
     * - PaymentIntent Stripe
     * - referencia bancaria
     */
    Optional<Pago> findByReferenciaTransaccion(
            String referenciaTransaccion
    );

    boolean existsByReferenciaTransaccion(
            String referenciaTransaccion
    );

    /*
     * =========================================================
     * SESSION STRIPE
     * =========================================================
     *
     * Idempotencia contra:
     * - webhooks duplicados
     * - reenvíos Stripe
     * - race conditions
     */
    Optional<Pago> findBySessionId(
            String sessionId
    );

    boolean existsBySessionId(
            String sessionId
    );
}