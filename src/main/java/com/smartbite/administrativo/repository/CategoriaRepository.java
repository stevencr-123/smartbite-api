package com.smartbite.administrativo.repository;

import com.smartbite.administrativo.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Optional<Categoria> findByNombre(String nombre);

    boolean existsByNombre(String nombre);

    boolean existsByNombreAndIdNot(String nombre, Long id);

    List<Categoria> findByCategoriaPadreId(Long categoriaPadreId);

    List<Categoria> findByActivoTrue();

    List<Categoria> findByCategoriaPadreIsNull();
}