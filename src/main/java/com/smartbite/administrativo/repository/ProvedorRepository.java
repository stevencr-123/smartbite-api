package com.smartbite.administrativo.repository;

import com.smartbite.administrativo.model.Provedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProvedorRepository extends JpaRepository<Provedor, Long> {

    Optional<Provedor> findByNit(String nit);

    Optional<Provedor> findByEmail(String email);

    boolean existsByNit(String nit);

    boolean existsByNitAndIdNot(String nit, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);

    List<Provedor> findByActivoTrue();

    List<Provedor> findByNombreContainingIgnoreCase(String nombre);
}