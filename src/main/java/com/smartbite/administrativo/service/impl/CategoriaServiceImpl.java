package com.smartbite.administrativo.service.impl;

import com.smartbite.administrativo.dto.CategoriaRequestDTO;
import com.smartbite.administrativo.dto.CategoriaResponseDTO;
import com.smartbite.administrativo.exception.BusinessException;
import com.smartbite.administrativo.exception.ResourceNotFoundException;
import com.smartbite.administrativo.model.Categoria;
import com.smartbite.administrativo.repository.CategoriaRepository;
import com.smartbite.administrativo.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Override
    public CategoriaResponseDTO crearCategoria(CategoriaRequestDTO requestDTO) {
        log.info("Creando categoría: {}", requestDTO.getNombre());

        // Validar nombre único
        if (categoriaRepository.existsByNombre(requestDTO.getNombre())) {
            throw new BusinessException("Ya existe una categoría con el nombre: " + requestDTO.getNombre());
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(requestDTO.getNombre());
        categoria.setDescripcion(requestDTO.getDescripcion());

        // Asignar categoría padre si existe
        if (requestDTO.getCategoriaPadreId() != null) {
            Categoria categoriaPadre = categoriaRepository.findById(requestDTO.getCategoriaPadreId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría padre no encontrada con ID: " + requestDTO.getCategoriaPadreId()));
            categoria.setCategoriaPadre(categoriaPadre);
        }

        Categoria guardado = categoriaRepository.save(categoria);
        log.info("Categoría creada con ID: {}", guardado.getId());

        return convertToResponseDTO(guardado);
    }

    @Override
    public CategoriaResponseDTO actualizarCategoria(Long id, CategoriaRequestDTO requestDTO) {
        log.info("Actualizando categoría con ID: {}", id);

        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));

        // Validar nombre único excluyendo la propia categoría
        if (categoriaRepository.existsByNombreAndIdNot(requestDTO.getNombre(), id)) {
            throw new BusinessException("Ya existe otra categoría con el nombre: " + requestDTO.getNombre());
        }

        categoria.setNombre(requestDTO.getNombre());
        categoria.setDescripcion(requestDTO.getDescripcion());

        // Actualizar categoría padre si cambia
        if (requestDTO.getCategoriaPadreId() != null) {
            // Validar que no se asigne a sí misma como padre
            if (requestDTO.getCategoriaPadreId().equals(id)) {
                throw new BusinessException("Una categoría no puede ser padre de sí misma");
            }

            Categoria nuevaCategoriaPadre = categoriaRepository.findById(requestDTO.getCategoriaPadreId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría padre no encontrada con ID: " + requestDTO.getCategoriaPadreId()));
            categoria.setCategoriaPadre(nuevaCategoriaPadre);
        } else {
            categoria.setCategoriaPadre(null);
        }

        Categoria actualizado = categoriaRepository.save(categoria);
        log.info("Categoría actualizada con ID: {}", actualizado.getId());

        return convertToResponseDTO(actualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaResponseDTO obtenerCategoriaPorId(Long id) {
        log.debug("Buscando categoría con ID: {}", id);

        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));

        return convertToResponseDTO(categoria);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> obtenerTodasLasCategorias() {
        log.debug("Obteniendo todas las categorías");

        return categoriaRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> obtenerCategoriasRaiz() {
        log.debug("Obteniendo categorías raíz (sin padre)");

        return categoriaRepository.findByCategoriaPadreIsNull().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> obtenerSubcategorias(Long categoriaPadreId) {
        log.debug("Obteniendo subcategorías de la categoría ID: {}", categoriaPadreId);

        return categoriaRepository.findByCategoriaPadreId(categoriaPadreId).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminarCategoria(Long id) {
        log.info("Eliminando categoría con ID: {}", id);

        if (!categoriaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Categoría no encontrada con ID: " + id);
        }

        categoriaRepository.deleteById(id);
        log.info("Categoría eliminada con ID: {}", id);
    }

    @Override
    public CategoriaResponseDTO activarDesactivarCategoria(Long id, Boolean activo) {
        log.info("Cambiando estado de categoría {} a activo={}", id, activo);

        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));

        categoria.setActivo(activo);
        Categoria actualizado = categoriaRepository.save(categoria);

        return convertToResponseDTO(actualizado);
    }


    private CategoriaResponseDTO convertToResponseDTO(Categoria categoria) {
        CategoriaResponseDTO responseDTO = new CategoriaResponseDTO();
        responseDTO.setId(categoria.getId());
        responseDTO.setNombre(categoria.getNombre());
        responseDTO.setDescripcion(categoria.getDescripcion());
        responseDTO.setActivo(categoria.getActivo());
        responseDTO.setFechaCreacion(categoria.getFechaCreacion());
        responseDTO.setFechaActualizacion(categoria.getFechaActualizacion());

        if (categoria.getCategoriaPadre() != null) {
            responseDTO.setCategoriaPadreId(categoria.getCategoriaPadre().getId());
            responseDTO.setCategoriaPadreNombre(categoria.getCategoriaPadre().getNombre());
        }

        return responseDTO;
    }
}