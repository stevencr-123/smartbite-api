package com.smartbite.operativo.controller;

import com.smartbite.operativo.dto.detalle.AgregarProductoRequestDTO;
import com.smartbite.operativo.dto.detalle.DetalleOrdenResponseDTO;
import com.smartbite.operativo.dto.orden.CrearOrdenRequestDTO;
import com.smartbite.operativo.dto.orden.OrdenResumenDTO;
import com.smartbite.operativo.dto.orden.OrdenResponseDTO;
import com.smartbite.operativo.model.enums.EstadoOrden;
import com.smartbite.operativo.service.OrdenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ordenes")
@RequiredArgsConstructor
public class OrdenController {

    private final OrdenService ordenService;

    @PostMapping
    public ResponseEntity<OrdenResponseDTO> crearOrden(@Valid @RequestBody CrearOrdenRequestDTO request) {
        OrdenResponseDTO response = ordenService.crearOrden(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenResponseDTO> obtenerOrdenPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ordenService.obtenerOrdenPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<OrdenResumenDTO>> listarOrdenes() {
        return ResponseEntity.ok(ordenService.obtenerOrdenesActivas());
    }

    // ✅ CORREGIDO: mejor práctica REST (body en vez de query param)
    @PutMapping("/{id}/estado")
    public ResponseEntity<OrdenResponseDTO> actualizarEstado(
            @PathVariable Long id,
            @RequestBody EstadoOrden estado) {
        return ResponseEntity.ok(ordenService.cambiarEstado(id, estado));
    }

    // ✅ NUEVO: agregar producto a la orden
    @PostMapping("/{id}/productos")
    public ResponseEntity<DetalleOrdenResponseDTO> agregarProducto(
            @PathVariable Long id,
            @Valid @RequestBody AgregarProductoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ordenService.agregarProducto(id, request));
    }

    // ✅ NUEVO: cerrar orden
    @PutMapping("/{id}/cerrar")
    public ResponseEntity<OrdenResponseDTO> cerrarOrden(@PathVariable Long id) {
        return ResponseEntity.ok(ordenService.cerrarOrden(id));
    }
}