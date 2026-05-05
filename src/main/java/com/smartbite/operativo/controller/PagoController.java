package com.smartbite.operativo.controller;

import com.smartbite.operativo.dto.pago.CrearPagoRequestDTO;
import com.smartbite.operativo.dto.pago.PagoResponseDTO;
import com.smartbite.operativo.service.PagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    @PostMapping
    public ResponseEntity<PagoResponseDTO> registrarPago(@Valid @RequestBody CrearPagoRequestDTO request) {
        PagoResponseDTO response = pagoService.registrarPago(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PagoResponseDTO> obtenerPagoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pagoService.obtenerPagoPorId(id));
    }

    // ✅ NUEVO: pagos por orden (clave para frontend)
    @GetMapping("/orden/{ordenId}")
    public ResponseEntity<List<PagoResponseDTO>> obtenerPagosPorOrden(@PathVariable Long ordenId) {
        return ResponseEntity.ok(pagoService.obtenerPagosPorOrden(ordenId));
    }
}