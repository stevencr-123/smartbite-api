package com.smartbite.administrativo.repository;

import com.smartbite.administrativo.model.Restaurante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RestauranteRepository extends JpaRepository<Restaurante, Long> {

    Optional<Restaurante> findByEmail(String email);

    boolean existsByNif(String nif);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByNifAndIdNot(String nif, Long id);
}