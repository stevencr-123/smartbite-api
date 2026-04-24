package com.smartbite.operativo.repository;

import com.smartbite.operativo.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

	boolean existsByOrdenId(Long ordenId);

	Optional<Venta> findByOrdenId(Long ordenId);
}

