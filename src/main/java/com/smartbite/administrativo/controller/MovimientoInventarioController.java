package com.smartbite.administrativo.controller;

import com.smartbite.administrativo.dto.MovimientoInventarioRequestDTO;
import com.smartbite.administrativo.dto.MovimientoInventarioResponseDTO;
import com.smartbite.administrativo.enums.TipoMovimientoInventario;
import com.smartbite.administrativo.service.MovimientoInventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/inventario/movimientos")
@RequiredArgsConstructor
public class MovimientoInventarioController {

    private final MovimientoInventarioService movimientoService;

    @PostMapping
    public ResponseEntity<MovimientoInventarioResponseDTO> registrar(@Valid @RequestBody MovimientoInventarioRequestDTO requestDTO) {
        MovimientoInventarioResponseDTO response = movimientoService.registrarMovimiento(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/entrada/{itemId}")
    public ResponseEntity<MovimientoInventarioResponseDTO> registrarEntrada(
            @PathVariable Long itemId,
            @RequestParam Integer cantidad,
            @RequestParam String motivo,
            @RequestParam(required = false) String referencia,
            @RequestParam(required = false) Long referenciaId) {
        MovimientoInventarioResponseDTO response = movimientoService.registrarEntrada(itemId, cantidad, motivo, referencia, referenciaId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/salida/{itemId}")
    public ResponseEntity<MovimientoInventarioResponseDTO> registrarSalida(
            @PathVariable Long itemId,
            @RequestParam Integer cantidad,
            @RequestParam String motivo,
            @RequestParam(required = false) String referencia,
            @RequestParam(required = false) Long referenciaId) {
        MovimientoInventarioResponseDTO response = movimientoService.registrarSalida(itemId, cantidad, motivo, referencia, referenciaId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/item/{itemInventarioId}")
    public ResponseEntity<List<MovimientoInventarioResponseDTO>> obtenerPorItem(@PathVariable Long itemInventarioId) {
        return ResponseEntity.ok(movimientoService.obtenerMovimientosPorItem(itemInventarioId));
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<MovimientoInventarioResponseDTO>> obtenerPorTipo(@PathVariable TipoMovimientoInventario tipo) {
        return ResponseEntity.ok(movimientoService.obtenerMovimientosPorTipo(tipo));
    }

    @GetMapping("/fechas")
    public ResponseEntity<List<MovimientoInventarioResponseDTO>> obtenerPorRangoFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return ResponseEntity.ok(movimientoService.obtenerMovimientosPorRangoFechas(inicio, fin));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovimientoInventarioResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(movimientoService.obtenerMovimientoPorId(id));
    }
}