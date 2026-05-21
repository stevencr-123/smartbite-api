package com.smartbite.operativo.client;

import com.smartbite.operativo.client.dto.ProductoDTO;
import com.smartbite.operativo.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Contrato obligatorio entre Operativo y Administrativo: GET /api/productos/{id}
@FeignClient(name = "adminProductoClient", url = "${admin.service.url}", configuration = FeignConfig.class)
public interface ProductoClient {

    @GetMapping("/api/productos/{id}")
    ProductoDTO obtenerProductoPorId(@PathVariable("id") Long id);
}

