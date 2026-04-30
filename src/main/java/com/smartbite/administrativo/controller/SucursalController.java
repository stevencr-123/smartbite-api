package com.smartbite.administrativo.controller;

import com.smartbite.administrativo.dto.SucursalRequestDTO;
import com.smartbite.administrativo.dto.SucursalResponseDTO;
import com.smartbite.administrativo.service.SucursalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sucursales")
@RequiredArgsConstructor
public class SucursalController {

    private final SucursalService sucursalService;

    @PostMapping
    public ResponseEntity<SucursalResponseDTO> crear(@Valid @RequestBody SucursalRequestDTO requestDTO) {
        SucursalResponseDTO response = sucursalService.crearSucursal(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SucursalResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody SucursalRequestDTO requestDTO) {
        SucursalResponseDTO response = sucursalService.actualizarSucursal(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SucursalResponseDTO> obtenerPorId(@PathVariable Long id) {
        SucursalResponseDTO response = sucursalService.obtenerSucursalPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<SucursalResponseDTO>> obtenerTodas() {
        List<SucursalResponseDTO> response = sucursalService.obtenerTodasLasSucursales();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/restaurante/{restauranteId}")
    public ResponseEntity<List<SucursalResponseDTO>> obtenerPorRestaurante(@PathVariable Long restauranteId) {
        List<SucursalResponseDTO> response = sucursalService.obtenerSucursalesPorRestaurante(restauranteId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        sucursalService.eliminarSucursal(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<SucursalResponseDTO> cambiarEstado(
            @PathVariable Long id,
            @RequestParam Boolean activo) {
        SucursalResponseDTO response = sucursalService.activarDesactivarSucursal(id, activo);
        return ResponseEntity.ok(response);
    }
}