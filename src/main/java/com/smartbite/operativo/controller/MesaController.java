package com.smartbite.operativo.controller;

import com.smartbite.operativo.dto.mesa.ActualizarEstadoMesaRequestDTO;
import com.smartbite.operativo.dto.mesa.CrearMesaRequestDTO;
import com.smartbite.operativo.dto.mesa.MesaResponseDTO;
import com.smartbite.operativo.model.enums.EstadoMesa;
import com.smartbite.operativo.service.MesaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mesas")
@RequiredArgsConstructor
public class MesaController {

    private final MesaService mesaService;

    @PostMapping
    public ResponseEntity<MesaResponseDTO> crearMesa(
            @Valid @RequestBody CrearMesaRequestDTO request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mesaService.crearMesa(request));
    }

    @GetMapping
    public ResponseEntity<List<MesaResponseDTO>> listarMesas(
            @RequestParam(value = "sucursalId", required = false) Long sucursalId,
            @RequestParam(value = "estado", required = false) EstadoMesa estado) {

        if (sucursalId != null && estado != null) {
            return ResponseEntity.ok(
                    mesaService.obtenerPorSucursalYEstado(sucursalId, estado)
            );
        }

        if (sucursalId != null) {
            return ResponseEntity.ok(mesaService.obtenerPorSucursal(sucursalId));
        }

        return ResponseEntity.ok(mesaService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MesaResponseDTO> obtenerMesaPorId(@PathVariable Long id) {

        return ResponseEntity.ok(mesaService.obtenerPorId(id));
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<MesaResponseDTO>> obtenerDisponibles(
            @RequestParam Long sucursalId) {

        return ResponseEntity.ok(
                mesaService.obtenerDisponibles(sucursalId)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<MesaResponseDTO> actualizarMesa(
            @PathVariable Long id,
            @Valid @RequestBody CrearMesaRequestDTO request) {

        return ResponseEntity.ok(
                mesaService.actualizarMesa(id, request)
        );
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<MesaResponseDTO> actualizarEstado(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarEstadoMesaRequestDTO request) {

        return ResponseEntity.ok(
                mesaService.actualizarEstado(id, request)
        );
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<MesaResponseDTO> activarMesa(@PathVariable Long id) {

        return ResponseEntity.ok(
                mesaService.cambiarEstadoActivo(id, true)
        );
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<MesaResponseDTO> desactivarMesa(@PathVariable Long id) {

        return ResponseEntity.ok(
                mesaService.cambiarEstadoActivo(id, false)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMesa(@PathVariable Long id) {

        mesaService.eliminarMesa(id);

        return ResponseEntity.noContent().build();
    }
}