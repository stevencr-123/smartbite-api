package com.smartbite.operativo.repository;

import com.smartbite.operativo.model.Orden;
import com.smartbite.operativo.model.enums.EstadoOrden;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrdenRepository
        extends JpaRepository<Orden, Long> {

    /*
     * =========================================================
     * ÓRDENES ACTIVAS
     * =========================================================
     *
     * 🔥 IMPORTANTE:
     * NO cargar simultáneamente:
     * - detalles
     * - pagos
     *
     * Hibernate NO soporta múltiples bags.
     */
    @EntityGraph(attributePaths = {
            "mesa",
            "cliente",
            "detalles"
    })
    List<Orden> findByEstadoIn(
            List<EstadoOrden> estados
    );

    /*
     * =========================================================
     * VALIDAR ORDEN ACTIVA POR MESA
     * =========================================================
     */
    @EntityGraph(attributePaths = {
            "mesa",
            "cliente",
            "detalles"
    })
    Optional<Orden> findByMesaIdAndEstadoIn(
            Long mesaId,
            List<EstadoOrden> estados
    );

    /*
     * =========================================================
     * OBTENER ORDEN COMPLETA
     * =========================================================
     */
    @Override
    @EntityGraph(attributePaths = {
            "mesa",
            "cliente",
            "detalles"
    })
    Optional<Orden> findById(
            Long id
    );
}