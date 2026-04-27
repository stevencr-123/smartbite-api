package com.smartbite.administrativo.repository;

import com.smartbite.administrativo.model.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PermisoRepository extends JpaRepository<Permiso, Long> {

    Optional<Permiso> findByNombre(String nombre);

    boolean existsByNombre(String nombre);

    boolean existsByNombreAndIdNot(String nombre, Long id);

    List<Permiso> findByRecurso(String recurso);

    List<Permiso> findByActivoTrue();

    List<Permiso> findByAccion(String accion);
}