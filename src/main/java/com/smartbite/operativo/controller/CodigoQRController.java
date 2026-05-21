package com.smartbite.operativo.controller;

import com.smartbite.operativo.dto.qr.CodigoQRResponseDTO;
import com.smartbite.operativo.dto.qr.GenerarQRRequestDTO;
import com.smartbite.operativo.service.CodigoQRService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/codigos-qr")
@RequiredArgsConstructor
public class CodigoQRController {

    private final CodigoQRService codigoQRService;

    // ✅ RECOMENDACIÓN: incluir ordenId en el DTO (si puedes)
    @PostMapping
    public ResponseEntity<CodigoQRResponseDTO> generarCodigoQr(
            @Valid @RequestBody GenerarQRRequestDTO request,
            @RequestParam(value = "ordenId", required = false) Long ordenId) {

        CodigoQRResponseDTO response = codigoQRService.generarQR(request, ordenId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CodigoQRResponseDTO> obtenerCodigoQrPorId(@PathVariable Long id) {
        return ResponseEntity.ok(codigoQRService.obtenerPorId(id));
    }
}