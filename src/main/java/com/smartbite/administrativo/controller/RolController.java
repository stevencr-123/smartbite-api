package com.smartbite.administrativo.controller;

import com.smartbite.administrativo.dto.RolRequestDTO;
import com.smartbite.administrativo.dto.RolResponseDTO;
import com.smartbite.administrativo.service.RolService;
import com.smartbite.security.constants.PermisoConstantes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RolController {

    private final RolService rolService;

    @PreAuthorize("hasAuthority('" + PermisoConstantes.CREAR_ROL + "')")
    @PostMapping
    public ResponseEntity<RolResponseDTO> crearRol(@Valid @RequestBody RolRequestDTO requestDTO) {
        return new ResponseEntity<>(rolService.crearRol(requestDTO), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('" + PermisoConstantes.ACTUALIZAR_ROL + "')")
    @PutMapping("/{id}")
    public ResponseEntity<RolResponseDTO> actualizarRol(
            @PathVariable Long id,
            @Valid @RequestBody RolRequestDTO requestDTO) {
        return ResponseEntity.ok(rolService.actualizarRol(id, requestDTO));
    }

    @PreAuthorize("hasAuthority('" + PermisoConstantes.VER_ROLES + "')")
    @GetMapping("/{id}")
    public ResponseEntity<RolResponseDTO> obtenerRolPorId(@PathVariable Long id) {
        return ResponseEntity.ok(rolService.obtenerRolPorId(id));
    }

    @PreAuthorize("hasAuthority('" + PermisoConstantes.VER_ROLES + "')")
    @GetMapping
    public ResponseEntity<List<RolResponseDTO>> obtenerTodosLosRoles() {
        return ResponseEntity.ok(rolService.obtenerTodosLosRoles());
    }

    @PreAuthorize("hasAuthority('" + PermisoConstantes.ELIMINAR_ROL + "')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarRol(@PathVariable Long id) {
        rolService.eliminarRol(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('" + PermisoConstantes.ACTUALIZAR_ROL + "')")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<RolResponseDTO> activarDesactivarRol(
            @PathVariable Long id,
            @RequestParam Boolean activo) {
        return ResponseEntity.ok(rolService.activarDesactivarRol(id, activo));
    }

    @PreAuthorize("hasAuthority('" + PermisoConstantes.ASIGNAR_PERMISO + "')")
    @PostMapping("/{rolId}/permisos")
    public ResponseEntity<RolResponseDTO> asignarPermisos(
            @PathVariable Long rolId,
            @RequestBody List<Long> permisosIds) {
        return ResponseEntity.ok(rolService.asignarPermisos(rolId, permisosIds));
    }

    @PreAuthorize("hasAuthority('" + PermisoConstantes.ASIGNAR_PERMISO + "')")
    @DeleteMapping("/{rolId}/permisos/{permisoId}")
    public ResponseEntity<RolResponseDTO> quitarPermiso(
            @PathVariable Long rolId,
            @PathVariable Long permisoId) {
        return ResponseEntity.ok(rolService.quitarPermiso(rolId, permisoId));
    }
}