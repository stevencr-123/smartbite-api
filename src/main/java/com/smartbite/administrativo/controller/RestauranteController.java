package com.smartbite.administrativo.controller;

import com.smartbite.administrativo.dto.RestauranteRequestDTO;
import com.smartbite.administrativo.dto.RestauranteResponseDTO;
import com.smartbite.administrativo.service.RestauranteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/restaurantes")
@RequiredArgsConstructor
public class RestauranteController {

    private final RestauranteService restauranteService;

    @PostMapping
    public ResponseEntity<RestauranteResponseDTO> crearRestaurante(@Valid @RequestBody RestauranteRequestDTO requestDTO) {
        RestauranteResponseDTO response = restauranteService.crearRestaurante(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestauranteResponseDTO> actualizarRestaurante(
            @PathVariable Long id,
            @Valid @RequestBody RestauranteRequestDTO requestDTO) {
        RestauranteResponseDTO response = restauranteService.actualizarRestaurante(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestauranteResponseDTO> obtenerRestaurante(@PathVariable Long id) {
        RestauranteResponseDTO response = restauranteService.obtenerRestaurantePorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<RestauranteResponseDTO>> obtenerTodosLosRestaurantes() {
        List<RestauranteResponseDTO> response = restauranteService.obtenerTodosLosRestaurantes();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarRestaurante(@PathVariable Long id) {
        restauranteService.eliminarRestaurante(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<RestauranteResponseDTO> cambiarEstado(
            @PathVariable Long id,
            @RequestParam Boolean activo) {
        RestauranteResponseDTO response = restauranteService.activarDesactivarRestaurante(id, activo);
        return ResponseEntity.ok(response);
    }
}