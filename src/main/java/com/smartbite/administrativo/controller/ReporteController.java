package com.smartbite.administrativo.controller;

import com.smartbite.administrativo.service.ReportePdfService;
import com.smartbite.security.constants.PermisoConstantes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReportePdfService reportePdfService;

    @PreAuthorize("hasAuthority('" + PermisoConstantes.GENERAR_REPORTE + "')")
    @GetMapping("/productos")
    public ResponseEntity<byte[]> generarReporteProductos() {
        try {
            byte[] pdf = reportePdfService.generarReporteProductos();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=reporte-productos.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PreAuthorize("hasAuthority('" + PermisoConstantes.GENERAR_REPORTE + "')")
    @GetMapping("/inventario")
    public ResponseEntity<byte[]> generarReporteInventario() {
        try {
            byte[] pdf = reportePdfService.generarReporteInventario();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=reporte-inventario.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PreAuthorize("hasAuthority('" + PermisoConstantes.GENERAR_REPORTE + "')")
    @GetMapping("/usuarios")
    public ResponseEntity<byte[]> generarReporteUsuarios() {
        try {
            byte[] pdf = reportePdfService.generarReporteUsuarios();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=reporte-usuarios.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PreAuthorize("hasAuthority('" + PermisoConstantes.GENERAR_REPORTE + "')")
    @GetMapping("/compras")
    public ResponseEntity<byte[]> generarReporteCompras() {
        try {
            byte[] pdf = reportePdfService.generarReporteCompras();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=reporte-compras.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}