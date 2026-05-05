package com.smartbite.operativo.controller;

import com.smartbite.operativo.dto.mesa.ActualizarEstadoMesaRequestDTO;
import com.smartbite.operativo.dto.mesa.MesaResponseDTO;
import com.smartbite.operativo.service.MesaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mesas")
@RequiredArgsConstructor
public class MesaController {

    private final MesaService mesaService;

    @GetMapping
    public ResponseEntity<List<MesaResponseDTO>> listarMesas(
            @RequestParam(value = "sucursalId", required = false) Long sucursalId) {
        if (sucursalId != null) {
            return ResponseEntity.ok(mesaService.obtenerPorSucursal(sucursalId));
        }
        return ResponseEntity.ok(mesaService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MesaResponseDTO> obtenerMesaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(mesaService.obtenerPorId(id));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<MesaResponseDTO> actualizarEstado(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarEstadoMesaRequestDTO request) {
        return ResponseEntity.ok(mesaService.actualizarEstado(id, request));
    }
}