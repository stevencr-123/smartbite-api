package com.smartbite.operativo.client;

import com.smartbite.administrativo.dto.SucursalResponseDTO;
import com.smartbite.operativo.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Contrato obligatorio entre Operativo y Administrativo: GET /api/sucursales/{id}
@FeignClient(name = "adminSucursalClient", url = "${admin.service.url}", configuration = FeignConfig.class)
public interface SucursalClient {

    @GetMapping("/api/sucursales/{id}")
    SucursalResponseDTO obtenerSucursalPorId(@PathVariable("id") Long id);
}

