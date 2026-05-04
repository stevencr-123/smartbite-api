package com.smartbite.administrativo.controller;

import com.smartbite.administrativo.dto.ItemInventarioRequestDTO;
import com.smartbite.administrativo.dto.ItemInventarioResponseDTO;
import com.smartbite.administrativo.service.ItemInventarioService;
import com.smartbite.security.constants.PermisoConstantes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
public class ItemInventarioController {

    private final ItemInventarioService itemInventarioService;

    // ==================== VER INVENTARIO (ADMIN, JEFE_INVENTARIO, JEFE_COCINA) ====================
    @PreAuthorize("hasAnyAuthority('" + PermisoConstantes.VER_INVENTARIO + "', '" + PermisoConstantes.VER_STOCK + "')")
    @GetMapping
    public ResponseEntity<List<ItemInventarioResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(itemInventarioService.obtenerTodosLosItems());
    }

    @PreAuthorize("hasAnyAuthority('" + PermisoConstantes.VER_INVENTARIO + "', '" + PermisoConstantes.VER_STOCK + "')")
    @GetMapping("/{id}")
    public ResponseEntity<ItemInventarioResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(itemInventarioService.obtenerItemPorId(id));
    }

    @PreAuthorize("hasAnyAuthority('" + PermisoConstantes.VER_INVENTARIO + "', '" + PermisoConstantes.VER_STOCK + "')")
    @GetMapping("/stock-bajo")
    public ResponseEntity<List<ItemInventarioResponseDTO>> obtenerStockBajo() {
        return ResponseEntity.ok(itemInventarioService.obtenerItemsConStockBajo());
    }

    // ==================== ACTUALIZAR STOCK (SOLO JEFE_INVENTARIO y ADMIN) ====================
    @PreAuthorize("hasAuthority('" + PermisoConstantes.ACTUALIZAR_STOCK + "')")
    @PatchMapping("/{id}/stock")
    public ResponseEntity<ItemInventarioResponseDTO> ajustarStock(
            @PathVariable Long id,
            @RequestParam Integer cantidad,
            @RequestParam String motivo) {
        return ResponseEntity.ok(itemInventarioService.ajustarStock(id, cantidad, motivo));
    }

    // ==================== CREAR/ACTUALIZAR ITEMS (SOLO JEFE_INVENTARIO y ADMIN) ====================
    @PreAuthorize("hasAuthority('" + PermisoConstantes.CREAR_PRODUCTO + "')")
    @PostMapping
    public ResponseEntity<ItemInventarioResponseDTO> crear(@Valid @RequestBody ItemInventarioRequestDTO requestDTO) {
        return new ResponseEntity<>(itemInventarioService.crearItem(requestDTO), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('" + PermisoConstantes.ACTUALIZAR_PRODUCTO + "')")
    @PutMapping("/{id}")
    public ResponseEntity<ItemInventarioResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ItemInventarioRequestDTO requestDTO) {
        return ResponseEntity.ok(itemInventarioService.actualizarItem(id, requestDTO));
    }
}