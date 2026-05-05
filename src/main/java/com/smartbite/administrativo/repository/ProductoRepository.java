package com.smartbite.administrativo.repository;

import com.smartbite.administrativo.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findByNombre(String nombre);

    boolean existsByNombre(String nombre);

    boolean existsByNombreAndIdNot(String nombre, Long id);

    List<Producto> findByCategoriaId(Long categoriaId);

    List<Producto> findBySucursalId(Long sucursalId);

    List<Producto> findByActivoTrue();

    List<Producto> findByDisponibleTrue();

    List<Producto> findBySucursalIdAndActivoTrue(Long sucursalId);

    List<Producto> findBySucursalIdAndDisponibleTrue(Long sucursalId);
}