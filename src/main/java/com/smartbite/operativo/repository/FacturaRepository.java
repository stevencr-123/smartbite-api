package com.smartbite.operativo.repository;

import com.smartbite.operativo.model.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {

	boolean existsByVentaId(Long ventaId);

	Optional<Factura> findByVentaId(Long ventaId);
}

