package com.smartbite.administrativo.service;

import com.smartbite.administrativo.dto.CategoriaRequestDTO;
import com.smartbite.administrativo.dto.CategoriaResponseDTO;
import java.util.List;

public interface CategoriaService {

    CategoriaResponseDTO crearCategoria(CategoriaRequestDTO requestDTO);

    CategoriaResponseDTO actualizarCategoria(Long id, CategoriaRequestDTO requestDTO);

    CategoriaResponseDTO obtenerCategoriaPorId(Long id);

    List<CategoriaResponseDTO> obtenerTodasLasCategorias();

    List<CategoriaResponseDTO> obtenerCategoriasRaiz();

    List<CategoriaResponseDTO> obtenerSubcategorias(Long categoriaPadreId);

    void eliminarCategoria(Long id);

    CategoriaResponseDTO activarDesactivarCategoria(Long id, Boolean activo);
}