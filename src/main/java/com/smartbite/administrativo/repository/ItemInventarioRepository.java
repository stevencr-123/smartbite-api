package com.smartbite.administrativo.repository;

import com.smartbite.administrativo.model.ItemInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ItemInventarioRepository extends JpaRepository<ItemInventario, Long> {

    Optional<ItemInventario> findByNombre(String nombre);

    boolean existsByNombre(String nombre);

    boolean existsByNombreAndIdNot(String nombre, Long id);

    List<ItemInventario> findBySucursalId(Long sucursalId);

    List<ItemInventario> findByActivoTrue();

    List<ItemInventario> findByStockActualLessThanEqual(Integer stockMinimo);

    List<ItemInventario> findBySucursalIdAndActivoTrue(Long sucursalId);
}