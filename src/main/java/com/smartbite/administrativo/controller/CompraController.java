package com.smartbite.administrativo.controller;

import com.smartbite.administrativo.dto.CompraRequestDTO;
import com.smartbite.administrativo.dto.CompraResponseDTO;
import com.smartbite.administrativo.dto.ReciboDetalleDTO;
import com.smartbite.administrativo.service.CompraService;
import com.smartbite.security.constants.PermisoConstantes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/compras")
@RequiredArgsConstructor
public class CompraController {

    private final CompraService compraService;

    @PreAuthorize("hasAuthority('" + PermisoConstantes.CREAR_COMPRA + "')")
    @PostMapping
    public ResponseEntity<CompraResponseDTO> crear(@Valid @RequestBody CompraRequestDTO requestDTO) {
        return new ResponseEntity<>(compraService.crearCompra(requestDTO), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyAuthority('" + PermisoConstantes.VER_COMPRAS + "', '" + PermisoConstantes.VER_REPORTES + "')")
    @GetMapping
    public ResponseEntity<List<CompraResponseDTO>> obtenerTodas() {
        return ResponseEntity.ok(compraService.obtenerTodasLasCompras());
    }

    @PreAuthorize("hasAnyAuthority('" + PermisoConstantes.VER_COMPRAS + "', '" + PermisoConstantes.VER_REPORTES + "')")
    @GetMapping("/{id}")
    public ResponseEntity<CompraResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(compraService.obtenerCompraPorId(id));
    }

    @PreAuthorize("hasAuthority('" + PermisoConstantes.RECIBIR_COMPRA + "')")
    @PostMapping("/{id}/recibir")
    public ResponseEntity<CompraResponseDTO> recibirCompra(
            @PathVariable Long id,
            @RequestBody List<ReciboDetalleDTO> recibos) {
        return ResponseEntity.ok(compraService.recibirCompra(id, recibos));
    }

    @PreAuthorize("hasAuthority('" + PermisoConstantes.ANULAR_COMPRA + "')")
    @PostMapping("/{id}/anular")
    public ResponseEntity<Void> anularCompra(@PathVariable Long id) {
        compraService.anularCompra(id);
        return ResponseEntity.noContent().build();
    }
}