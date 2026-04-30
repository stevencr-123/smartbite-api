package com.smartbite.administrativo.controller;

import com.smartbite.administrativo.dto.UsuarioRequestDTO;
import com.smartbite.administrativo.dto.UsuarioResponseDTO;
import com.smartbite.administrativo.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> crear(@Valid @RequestBody UsuarioRequestDTO requestDTO) {
        UsuarioResponseDTO response = usuarioService.crearUsuario(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRequestDTO requestDTO) {
        UsuarioResponseDTO response = usuarioService.actualizarUsuario(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> obtenerPorId(@PathVariable Long id) {
        UsuarioResponseDTO response = usuarioService.obtenerUsuarioPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> obtenerTodos() {
        List<UsuarioResponseDTO> response = usuarioService.obtenerTodosLosUsuarios();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sucursal/{sucursalId}")
    public ResponseEntity<List<UsuarioResponseDTO>> obtenerPorSucursal(@PathVariable Long sucursalId) {
        List<UsuarioResponseDTO> response = usuarioService.obtenerUsuariosPorSucursal(sucursalId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/rol/{rolId}")
    public ResponseEntity<List<UsuarioResponseDTO>> obtenerPorRol(@PathVariable Long rolId) {
        List<UsuarioResponseDTO> response = usuarioService.obtenerUsuariosPorRol(rolId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<UsuarioResponseDTO> cambiarEstado(
            @PathVariable Long id,
            @RequestParam Boolean activo) {
        UsuarioResponseDTO response = usuarioService.activarDesactivarUsuario(id, activo);
        return ResponseEntity.ok(response);
    }
}