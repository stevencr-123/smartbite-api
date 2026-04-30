package com.smartbite.administrativo.repository;

import com.smartbite.administrativo.model.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SucursalRepository extends JpaRepository<Sucursal, Long> {

    List<Sucursal> findByRestauranteId(Long restauranteId);

    Optional<Sucursal> findByEmail(String email);

    boolean existsByNombreAndRestauranteId(String nombre, Long restauranteId);

    boolean existsByEmailAndIdNot(String email, Long id);

    List<Sucursal> findByActivoTrue();
}