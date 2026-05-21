package com.smartbite.operativo.repository;

import com.smartbite.operativo.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository
        extends JpaRepository<Cliente, Long> {

    boolean existsByNumeroDocumento(
            String numeroDocumento
    );

    Optional<Cliente> findByNumeroDocumento(
            String numeroDocumento
    );
}