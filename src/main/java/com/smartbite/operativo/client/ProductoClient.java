package com.smartbite.operativo.client;

import com.smartbite.operativo.client.dto.ProductoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "productoClient", url = "${admin.service.url}")
public interface ProductoClient {

    @GetMapping("/productos/{id}")
    ProductoDTO obtenerProductoPorId(@PathVariable("id") Long id);
}

