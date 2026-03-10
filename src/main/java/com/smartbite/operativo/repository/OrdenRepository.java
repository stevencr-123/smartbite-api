package com.smartbite.operativo.repository;

import com.smartbite.operativo.model.Orden;
import com.smartbite.operativo.model.enums.EstadoOrden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrdenRepository extends JpaRepository<Orden, Long> {

    List<Orden> findByEstadoIn(List<EstadoOrden> estados);

    Optional<Orden> findByMesaIdAndEstadoIn(Long mesaId, List<EstadoOrden> estados);
}

