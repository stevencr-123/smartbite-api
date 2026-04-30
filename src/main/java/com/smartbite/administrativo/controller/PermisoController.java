package com.smartbite.administrativo.controller;

import com.smartbite.administrativo.dto.PermisoRequestDTO;
import com.smartbite.administrativo.dto.PermisoResponseDTO;
import com.smartbite.administrativo.service.PermisoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permisos")
@RequiredArgsConstructor
public class PermisoController {

    private final PermisoService permisoService;

    @PostMapping
    public ResponseEntity<PermisoResponseDTO> crearPermiso(@Valid @RequestBody PermisoRequestDTO requestDTO) {
        PermisoResponseDTO response = permisoService.crearPermiso(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PermisoResponseDTO> actualizarPermiso(
            @PathVariable Long id,
            @Valid @RequestBody PermisoRequestDTO requestDTO) {
        PermisoResponseDTO response = permisoService.actualizarPermiso(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PermisoResponseDTO> obtenerPermiso(@PathVariable Long id) {
        PermisoResponseDTO response = permisoService.obtenerPermisoPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PermisoResponseDTO>> obtenerTodos() {
        List<PermisoResponseDTO> response = permisoService.obtenerTodosLosPermisos();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recurso/{recurso}")
    public ResponseEntity<List<PermisoResponseDTO>> obtenerPorRecurso(@PathVariable String recurso) {
        List<PermisoResponseDTO> response = permisoService.obtenerPermisosPorRecurso(recurso);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/accion/{accion}")
    public ResponseEntity<List<PermisoResponseDTO>> obtenerPorAccion(@PathVariable String accion) {
        List<PermisoResponseDTO> response = permisoService.obtenerPermisosPorAccion(accion);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPermiso(@PathVariable Long id) {
        permisoService.eliminarPermiso(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<PermisoResponseDTO> cambiarEstado(
            @PathVariable Long id,
            @RequestParam Boolean activo) {
        PermisoResponseDTO response = permisoService.activarDesactivarPermiso(id, activo);
        return ResponseEntity.ok(response);
    }
}