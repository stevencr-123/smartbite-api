package com.smartbite.administrativo.controller;

import com.smartbite.administrativo.dto.CategoriaRequestDTO;
import com.smartbite.administrativo.dto.CategoriaResponseDTO;
import com.smartbite.administrativo.service.CategoriaService;
import com.smartbite.security.constants.PermisoConstantes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    // ==================== TODOS LOS ROLES AUTENTICADOS ====================
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<CategoriaResponseDTO>> obtenerTodas() {
        return ResponseEntity.ok(categoriaService.obtenerTodasLasCategorias());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.obtenerCategoriaPorId(id));
    }

    // ==================== JEFE_COCINA y ADMIN pueden crear/actualizar ====================
    @PreAuthorize("hasAnyAuthority('" + PermisoConstantes.CREAR_CATEGORIA + "')")
    @PostMapping
    public ResponseEntity<CategoriaResponseDTO> crear(@Valid @RequestBody CategoriaRequestDTO requestDTO) {
        return new ResponseEntity<>(categoriaService.crearCategoria(requestDTO), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyAuthority('" + PermisoConstantes.ACTUALIZAR_CATEGORIA + "')")
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaRequestDTO requestDTO) {
        return ResponseEntity.ok(categoriaService.actualizarCategoria(id, requestDTO));
    }

    @PreAuthorize("hasAnyAuthority('" + PermisoConstantes.ELIMINAR_CATEGORIA + "')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        categoriaService.eliminarCategoria(id);
        return ResponseEntity.noContent().build();
    }
}