package com.smartbite.operativo.controller;

import com.smartbite.operativo.dto.metodopago.MetodoPagoResponseDTO;
import com.smartbite.operativo.service.MetodoPagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/metodos-pago")
@RequiredArgsConstructor
public class MetodoPagoController {

    private final MetodoPagoService metodoPagoService;

    @GetMapping
    public ResponseEntity<List<MetodoPagoResponseDTO>> obtenerActivos() {

        return ResponseEntity.ok(
                metodoPagoService.obtenerActivos()
        );
    }

    @GetMapping("/todos")
    public ResponseEntity<List<MetodoPagoResponseDTO>> obtenerTodos() {

        return ResponseEntity.ok(
                metodoPagoService.obtenerTodos()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<MetodoPagoResponseDTO> obtenerPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                metodoPagoService.obtenerPorId(id)
        );
    }
}