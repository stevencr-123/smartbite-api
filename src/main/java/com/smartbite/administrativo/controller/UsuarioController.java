package com.smartbite.administrativo.controller;

import com.smartbite.administrativo.dto.UsuarioRequestDTO;
import com.smartbite.administrativo.dto.UsuarioResponseDTO;
import com.smartbite.administrativo.enums.RolNombre;
import com.smartbite.administrativo.service.UsuarioService;
import com.smartbite.security.constants.PermisoConstantes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    // ==================== SOLO ADMINISTRADOR ====================

    @PreAuthorize("hasAuthority('" + PermisoConstantes.CREAR_USUARIO + "')")
    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> crear(@Valid @RequestBody UsuarioRequestDTO requestDTO) {
        UsuarioResponseDTO response = usuarioService.crearUsuario(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('" + PermisoConstantes.VER_USUARIOS + "')")
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(usuarioService.obtenerTodosLosUsuarios());
    }

    @PreAuthorize("hasAuthority('" + PermisoConstantes.VER_USUARIOS + "')")
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerUsuarioPorId(id));
    }

    @PreAuthorize("hasAuthority('" + PermisoConstantes.ACTUALIZAR_USUARIO + "')")
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRequestDTO requestDTO) {
        return ResponseEntity.ok(usuarioService.actualizarUsuario(id, requestDTO));
    }

    @PreAuthorize("hasAuthority('" + PermisoConstantes.ELIMINAR_USUARIO + "')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('" + PermisoConstantes.ACTUALIZAR_USUARIO + "')")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<UsuarioResponseDTO> cambiarEstado(
            @PathVariable Long id,
            @RequestParam Boolean activo) {
        return ResponseEntity.ok(usuarioService.activarDesactivarUsuario(id, activo));
    }

    @PreAuthorize("hasAuthority('" + PermisoConstantes.ASIGNAR_ROL + "')")
    @PatchMapping("/{id}/rol")
    public ResponseEntity<UsuarioResponseDTO> asignarRol(
            @PathVariable Long id,
            @RequestParam Long rolId) {
        return ResponseEntity.ok(usuarioService.asignarRol(id, rolId));
    }

    // ==================== ADMINISTRADOR o JEFE_INVENTARIO ====================

    @PreAuthorize("hasAnyAuthority('" + PermisoConstantes.VER_USUARIOS + "', '" + PermisoConstantes.VER_USUARIOS_SUCURSAL + "')")
    @GetMapping("/sucursal/{sucursalId}")
    public ResponseEntity<List<UsuarioResponseDTO>> obtenerPorSucursal(@PathVariable Long sucursalId) {
        return ResponseEntity.ok(usuarioService.obtenerUsuariosPorSucursal(sucursalId));
    }

    // ==================== ADMINISTRADOR o JEFE_COCINA ====================

    @PreAuthorize("hasAnyAuthority('" + PermisoConstantes.VER_USUARIOS + "', '" + PermisoConstantes.VER_ROLES + "')")
    @GetMapping("/rol/{rolNombre}")
    public ResponseEntity<List<UsuarioResponseDTO>> obtenerPorRol(@PathVariable String rolNombre) {
        RolNombre rol;
        try {
            rol = RolNombre.valueOf(rolNombre.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Rol no válido: " + rolNombre);
        }
        return ResponseEntity.ok(usuarioService.obtenerUsuariosPorRol(rol));
    }
}