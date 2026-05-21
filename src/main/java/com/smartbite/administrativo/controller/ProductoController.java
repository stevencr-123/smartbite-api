package com.smartbite.administrativo.controller;

import com.smartbite.administrativo.dto.ProductoRequestDTO;
import com.smartbite.administrativo.dto.ProductoResponseDTO;
import com.smartbite.administrativo.service.ProductoService;
import com.smartbite.security.constants.PermisoConstantes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    // ==================== TODOS LOS ROLES AUTENTICADOS ====================
    // @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<ProductoResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(productoService.obtenerTodosLosProductos());
    }

    // @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerProductoPorId(id));
    }

    // ==================== MESERO puede ver solo disponibles ====================
    @PreAuthorize("hasAnyAuthority('" + PermisoConstantes.VER_PRODUCTOS_DISPONIBLES + "')")
    @GetMapping("/disponibles")
    public ResponseEntity<List<ProductoResponseDTO>> obtenerDisponibles() {
        return ResponseEntity.ok(productoService.obtenerProductosDisponibles());
    }

    // ==================== JEFE_COCINA y ADMIN pueden crear/actualizar ====================
    @PreAuthorize("hasAnyAuthority('" + PermisoConstantes.CREAR_PRODUCTO + "')")
    @PostMapping
    public ResponseEntity<ProductoResponseDTO> crear(@Valid @RequestBody ProductoRequestDTO requestDTO) {
        return new ResponseEntity<>(productoService.crearProducto(requestDTO), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyAuthority('" + PermisoConstantes.ACTUALIZAR_PRODUCTO + "')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProductoRequestDTO requestDTO) {
        return ResponseEntity.ok(productoService.actualizarProducto(id, requestDTO));
    }

    @PreAuthorize("hasAnyAuthority('" + PermisoConstantes.ELIMINAR_PRODUCTO + "')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority('" + PermisoConstantes.CAMBIAR_DISPONIBILIDAD + "')")
    @PatchMapping("/{id}/disponibilidad")
    public ResponseEntity<ProductoResponseDTO> cambiarDisponibilidad(
            @PathVariable Long id,
            @RequestParam Boolean disponible) {
        return ResponseEntity.ok(productoService.cambiarDisponibilidad(id, disponible));
    }
}