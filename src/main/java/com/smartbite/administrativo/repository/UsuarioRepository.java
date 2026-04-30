package com.smartbite.administrativo.repository;

import com.smartbite.administrativo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    List<Usuario> findBySucursalId(Long sucursalId);

    List<Usuario> findByRolId(Long rolId);

    List<Usuario> findByActivoTrue();
}