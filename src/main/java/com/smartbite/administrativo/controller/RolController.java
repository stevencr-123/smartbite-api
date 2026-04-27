package com.smartbite.administrativo.controller;

import com.smartbite.administrativo.dto.RolRequestDTO;
import com.smartbite.administrativo.dto.RolResponseDTO;
import com.smartbite.administrativo.service.RolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RolController {

    private final RolService rolService;

    @PostMapping
    public ResponseEntity<RolResponseDTO> crearRol(@Valid @RequestBody RolRequestDTO requestDTO) {
        RolResponseDTO response = rolService.crearRol(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RolResponseDTO> actualizarRol(
            @PathVariable Long id,
            @Valid @RequestBody RolRequestDTO requestDTO) {
        RolResponseDTO response = rolService.actualizarRol(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RolResponseDTO> obtenerRolPorId(@PathVariable Long id) {
        RolResponseDTO response = rolService.obtenerRolPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<RolResponseDTO>> obtenerTodosLosRoles() {
        List<RolResponseDTO> response = rolService.obtenerTodosLosRoles();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarRol(@PathVariable Long id) {
        rolService.eliminarRol(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<RolResponseDTO> activarDesactivarRol(
            @PathVariable Long id,
            @RequestParam Boolean activo) {
        RolResponseDTO response = rolService.activarDesactivarRol(id, activo);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{rolId}/permisos")
    public ResponseEntity<RolResponseDTO> asignarPermisos(
            @PathVariable Long rolId,
            @RequestBody List<Long> permisosIds) {
        RolResponseDTO response = rolService.asignarPermisos(rolId, permisosIds);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{rolId}/permisos/{permisoId}")
    public ResponseEntity<RolResponseDTO> quitarPermiso(
            @PathVariable Long rolId,
            @PathVariable Long permisoId) {
        RolResponseDTO response = rolService.quitarPermiso(rolId, permisoId);
        return ResponseEntity.ok(response);
    }
}