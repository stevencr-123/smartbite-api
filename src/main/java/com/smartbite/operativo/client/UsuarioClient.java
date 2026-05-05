package com.smartbite.operativo.client;

import com.smartbite.administrativo.dto.UsuarioResponseDTO;
import com.smartbite.operativo.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Contrato obligatorio entre Operativo y Administrativo: GET /api/usuarios/{id}
@FeignClient(name = "adminUsuarioClient", url = "${admin.service.url}", configuration = FeignConfig.class)
public interface UsuarioClient {

    @GetMapping("/api/usuarios/{id}")
    UsuarioResponseDTO obtenerUsuarioPorId(@PathVariable("id") Long id);
}

