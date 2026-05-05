package com.smartbite.administrativo.repository;

import com.smartbite.administrativo.enums.EstadoCompra;
import com.smartbite.administrativo.model.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {

    List<Compra> findByProvedorId(Long provedorId);

    List<Compra> findBySucursalId(Long sucursalId);

    List<Compra> findByEstado(EstadoCompra estado);

    List<Compra> findByFechaCompraBetween(LocalDateTime inicio, LocalDateTime fin);
}