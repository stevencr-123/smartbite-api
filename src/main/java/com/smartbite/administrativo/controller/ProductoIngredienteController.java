package com.smartbite.administrativo.controller;

import com.smartbite.administrativo.dto.ProductoIngredienteRequestDTO;
import com.smartbite.administrativo.dto.ProductoIngredienteResponseDTO;
import com.smartbite.administrativo.service.ProductoIngredienteService;
import com.smartbite.security.constants.PermisoConstantes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos-ingredientes")
@RequiredArgsConstructor
public class ProductoIngredienteController {

    private final ProductoIngredienteService productoIngredienteService;

    // ==================== VER RECETAS ====================
    @PreAuthorize("hasAnyAuthority('" + PermisoConstantes.VER_RECETAS + "', '" + PermisoConstantes.VER_PRODUCTOS + "')")
    @GetMapping
    public ResponseEntity<List<ProductoIngredienteResponseDTO>> obtenerTodas() {
        return ResponseEntity.ok(productoIngredienteService.obtenerRelacionesPorProducto(null));
    }

    @PreAuthorize("hasAnyAuthority('" + PermisoConstantes.VER_RECETAS + "', '" + PermisoConstantes.VER_PRODUCTOS + "')")
    @GetMapping("/{id}")
    public ResponseEntity<ProductoIngredienteResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoIngredienteService.obtenerRelacionPorId(id));
    }

    @PreAuthorize("hasAnyAuthority('" + PermisoConstantes.VER_RECETAS + "', '" + PermisoConstantes.VER_PRODUCTOS + "')")
    @GetMapping("/producto/{productoId}")
    public ResponseEntity<List<ProductoIngredienteResponseDTO>> obtenerPorProducto(@PathVariable Long productoId) {
        return ResponseEntity.ok(productoIngredienteService.obtenerRelacionesPorProducto(productoId));
    }

    // ==================== CREAR/ACTUALIZAR RECETAS (JEFE_COCINA y ADMIN) ====================
    @PreAuthorize("hasAuthority('" + PermisoConstantes.CREAR_RECETA + "')")
    @PostMapping
    public ResponseEntity<ProductoIngredienteResponseDTO> crear(@Valid @RequestBody ProductoIngredienteRequestDTO requestDTO) {
        return new ResponseEntity<>(productoIngredienteService.crearRelacion(requestDTO), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('" + PermisoConstantes.ACTUALIZAR_RECETA + "')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductoIngredienteResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProductoIngredienteRequestDTO requestDTO) {
        return ResponseEntity.ok(productoIngredienteService.actualizarRelacion(id, requestDTO));
    }

    @PreAuthorize("hasAuthority('" + PermisoConstantes.ELIMINAR_RECETA + "')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoIngredienteService.eliminarRelacion(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority('" + PermisoConstantes.VER_RECETAS + "', '" + PermisoConstantes.VER_PRODUCTOS + "')")
    @GetMapping("/producto/{productoId}/costo")
    public ResponseEntity<Double> calcularCostoProducto(@PathVariable Long productoId) {
        return ResponseEntity.ok(productoIngredienteService.calcularCostoProducto(productoId));
    }
}