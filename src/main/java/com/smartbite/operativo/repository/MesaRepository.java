package com.smartbite.operativo.repository;

import com.smartbite.operativo.model.Mesa;
import com.smartbite.operativo.model.enums.EstadoMesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MesaRepository extends JpaRepository<Mesa, Long> {

    List<Mesa> findBySucursalId(Long sucursalId);

    List<Mesa> findBySucursalIdAndEstado(
            Long sucursalId,
            EstadoMesa estado
    );

    List<Mesa> findBySucursalIdAndEstadoAndActivaTrue(
            Long sucursalId,
            EstadoMesa estado
    );
}