package com.smartbite.administrativo.repository;

import com.smartbite.administrativo.model.ProductoIngrediente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoIngredienteRepository extends JpaRepository<ProductoIngrediente, Long> {

    List<ProductoIngrediente> findByProductoId(Long productoId);

    List<ProductoIngrediente> findByItemInventarioId(Long itemInventarioId);

    Optional<ProductoIngrediente> findByProductoIdAndItemInventarioId(Long productoId, Long itemInventarioId);

    List<ProductoIngrediente> findByProductoIdAndActivoTrue(Long productoId);

    List<ProductoIngrediente> findByItemInventarioIdAndActivoTrue(Long itemInventarioId);

    boolean existsByProductoIdAndItemInventarioId(Long productoId, Long itemInventarioId);

    void deleteByProductoId(Long productoId);
}