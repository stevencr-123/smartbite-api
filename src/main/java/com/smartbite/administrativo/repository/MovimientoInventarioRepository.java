package com.smartbite.administrativo.repository;

import com.smartbite.administrativo.enums.TipoMovimientoInventario;
import com.smartbite.administrativo.model.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    List<MovimientoInventario> findByItemInventarioId(Long itemInventarioId);

    List<MovimientoInventario> findByTipo(TipoMovimientoInventario tipo);

    List<MovimientoInventario> findByFechaMovimientoBetween(LocalDateTime inicio, LocalDateTime fin);
}