package com.smartbite.administrativo.controller;

import com.smartbite.administrativo.dto.ProvedorRequestDTO;
import com.smartbite.administrativo.dto.ProvedorResponseDTO;
import com.smartbite.administrativo.service.ProvedorService;
import com.smartbite.security.constants.PermisoConstantes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/provedores")
@RequiredArgsConstructor
public class ProvedorController {

    private final ProvedorService provedorService;

    @PreAuthorize("hasAnyAuthority('" + PermisoConstantes.VER_PROVEEDORES + "', '" + PermisoConstantes.VER_REPORTES + "')")
    @GetMapping
    public ResponseEntity<List<ProvedorResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(provedorService.obtenerTodosLosProvedores());
    }

    @PreAuthorize("hasAnyAuthority('" + PermisoConstantes.VER_PROVEEDORES + "', '" + PermisoConstantes.VER_REPORTES + "')")
    @GetMapping("/{id}")
    public ResponseEntity<ProvedorResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(provedorService.obtenerProvedorPorId(id));
    }

    @PreAuthorize("hasAuthority('" + PermisoConstantes.CREAR_PROVEEDOR + "')")
    @PostMapping
    public ResponseEntity<ProvedorResponseDTO> crear(@Valid @RequestBody ProvedorRequestDTO requestDTO) {
        return new ResponseEntity<>(provedorService.crearProvedor(requestDTO), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('" + PermisoConstantes.ACTUALIZAR_PROVEEDOR + "')")
    @PutMapping("/{id}")
    public ResponseEntity<ProvedorResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProvedorRequestDTO requestDTO) {
        return ResponseEntity.ok(provedorService.actualizarProvedor(id, requestDTO));
    }

    @PreAuthorize("hasAuthority('" + PermisoConstantes.ELIMINAR_PROVEEDOR + "')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        provedorService.eliminarProvedor(id);
        return ResponseEntity.noContent().build();
    }
}