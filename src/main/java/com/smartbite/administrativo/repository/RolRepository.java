package com.smartbite.administrativo.repository;

import com.smartbite.administrativo.enums.RolNombre;
import com.smartbite.administrativo.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    Optional<Rol> findByNombre(RolNombre nombre);  

    boolean existsByNombre(RolNombre nombre);

    boolean existsByNombreAndIdNot(RolNombre nombre, Long id);
}