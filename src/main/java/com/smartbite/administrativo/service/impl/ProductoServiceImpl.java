package com.smartbite.administrativo.service.impl;

import com.smartbite.administrativo.dto.ProductoRequestDTO;
import com.smartbite.administrativo.dto.ProductoResponseDTO;
import com.smartbite.administrativo.exception.BusinessException;
import com.smartbite.administrativo.exception.ResourceNotFoundException;
import com.smartbite.administrativo.mapper.ProductoMapper;
import com.smartbite.administrativo.model.Categoria;
import com.smartbite.administrativo.model.Producto;
import com.smartbite.administrativo.model.Sucursal;
import com.smartbite.administrativo.repository.CategoriaRepository;
import com.smartbite.administrativo.repository.ProductoRepository;
import com.smartbite.administrativo.repository.SucursalRepository;
import com.smartbite.administrativo.service.ProductoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final SucursalRepository sucursalRepository;
    private final ProductoMapper productoMapper;

    @Override
    public ProductoResponseDTO crearProducto(ProductoRequestDTO requestDTO) {
        log.info("Creando producto: {}", requestDTO.getNombre());

        // Validar precio (CRÍTICO: no nulo, no negativo, no cero)
        if (requestDTO.getPrecio() == null) {
            throw new BusinessException("El precio del producto es obligatorio");
        }
        if (requestDTO.getPrecio() <= 0) {
            throw new BusinessException("El precio del producto debe ser mayor a 0");
        }

        // Validar nombre único
        if (productoRepository.existsByNombre(requestDTO.getNombre())) {
            throw new BusinessException("Ya existe un producto con el nombre: " + requestDTO.getNombre());
        }

        // Validar que la categoría existe y está activa
        Categoria categoria = categoriaRepository.findById(requestDTO.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + requestDTO.getCategoriaId()));

        if (!categoria.getActivo()) {
            throw new BusinessException("No se puede asignar un producto a una categoría inactiva");
        }

        // Validar que la sucursal existe y está activa
        Sucursal sucursal = sucursalRepository.findById(requestDTO.getSucursalId())
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada con ID: " + requestDTO.getSucursalId()));

        if (!sucursal.getActivo()) {
            throw new BusinessException("No se puede asignar un producto a una sucursal inactiva");
        }

        // Convertir DTO a Entity
        Producto producto = productoMapper.toEntity(requestDTO);

        // Asignar relaciones manualmente
        producto.setCategoria(categoria);
        producto.setSucursal(sucursal);

        // El precio ya viene del DTO
        producto.setPrecio(requestDTO.getPrecio());

        Producto guardado = productoRepository.save(producto);
        log.info("Producto creado con ID: {}, Precio: {}", guardado.getId(), guardado.getPrecio());

        return productoMapper.toResponseDTO(guardado);
    }

    @Override
    public ProductoResponseDTO actualizarProducto(Long id, ProductoRequestDTO requestDTO) {
        log.info("Actualizando producto con ID: {}", id);

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

        // Validar precio (CRÍTICO)
        if (requestDTO.getPrecio() == null) {
            throw new BusinessException("El precio del producto es obligatorio");
        }
        if (requestDTO.getPrecio() <= 0) {
            throw new BusinessException("El precio del producto debe ser mayor a 0");
        }

        // Validar nombre único excluyendo el propio producto
        if (productoRepository.existsByNombreAndIdNot(requestDTO.getNombre(), id)) {
            throw new BusinessException("Ya existe otro producto con el nombre: " + requestDTO.getNombre());
        }

        // Actualizar campos básicos
        producto.setNombre(requestDTO.getNombre());
        producto.setDescripcion(requestDTO.getDescripcion());
        producto.setPrecio(requestDTO.getPrecio());  // CRÍTICO
        producto.setUrlImagen(requestDTO.getUrlImagen());

        // Actualizar categoría si cambia
        if (!producto.getCategoria().getId().equals(requestDTO.getCategoriaId())) {
            Categoria nuevaCategoria = categoriaRepository.findById(requestDTO.getCategoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + requestDTO.getCategoriaId()));

            if (!nuevaCategoria.getActivo()) {
                throw new BusinessException("No se puede asignar un producto a una categoría inactiva");
            }
            producto.setCategoria(nuevaCategoria);
        }

        // Actualizar sucursal si cambia
        if (!producto.getSucursal().getId().equals(requestDTO.getSucursalId())) {
            Sucursal nuevaSucursal = sucursalRepository.findById(requestDTO.getSucursalId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada con ID: " + requestDTO.getSucursalId()));

            if (!nuevaSucursal.getActivo()) {
                throw new BusinessException("No se puede asignar un producto a una sucursal inactiva");
            }
            producto.setSucursal(nuevaSucursal);
        }

        Producto actualizado = productoRepository.save(producto);
        log.info("Producto actualizado con ID: {}, Nuevo precio: {}", actualizado.getId(), actualizado.getPrecio());

        return productoMapper.toResponseDTO(actualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponseDTO obtenerProductoPorId(Long id) {
        log.debug("Buscando producto con ID: {}", id);

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

        return productoMapper.toResponseDTO(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> obtenerTodosLosProductos() {
        log.debug("Obteniendo todos los productos");

        return productoRepository.findAll().stream()
                .map(productoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> obtenerProductosPorCategoria(Long categoriaId) {
        log.debug("Obteniendo productos de la categoría ID: {}", categoriaId);

        if (!categoriaRepository.existsById(categoriaId)) {
            throw new ResourceNotFoundException("Categoría no encontrada con ID: " + categoriaId);
        }

        return productoRepository.findByCategoriaId(categoriaId).stream()
                .map(productoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> obtenerProductosPorSucursal(Long sucursalId) {
        log.debug("Obteniendo productos de la sucursal ID: {}", sucursalId);

        if (!sucursalRepository.existsById(sucursalId)) {
            throw new ResourceNotFoundException("Sucursal no encontrada con ID: " + sucursalId);
        }

        return productoRepository.findBySucursalId(sucursalId).stream()
                .map(productoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> obtenerProductosActivos() {
        log.debug("Obteniendo productos activos");

        return productoRepository.findByActivoTrue().stream()
                .map(productoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> obtenerProductosDisponibles() {
        log.debug("Obteniendo productos disponibles");

        return productoRepository.findByDisponibleTrue().stream()
                .map(productoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> obtenerProductosDisponiblesPorSucursal(Long sucursalId) {
        log.debug("Obteniendo productos disponibles de la sucursal ID: {}", sucursalId);

        if (!sucursalRepository.existsById(sucursalId)) {
            throw new ResourceNotFoundException("Sucursal no encontrada con ID: " + sucursalId);
        }

        return productoRepository.findBySucursalIdAndDisponibleTrue(sucursalId).stream()
                .map(productoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminarProducto(Long id) {
        log.info("Eliminando (desactivando) producto con ID: {}", id);

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

        producto.setActivo(false);
        producto.setDisponible(false);
        productoRepository.save(producto);
        log.info("Producto desactivado con ID: {}", id);
    }

    @Override
    public ProductoResponseDTO activarDesactivarProducto(Long id, Boolean activo) {
        log.info("Cambiando estado del producto {} a activo={}", id, activo);

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

        producto.setActivo(activo);
        if (!activo) {
            producto.setDisponible(false);
        }
        Producto actualizado = productoRepository.save(producto);

        return productoMapper.toResponseDTO(actualizado);
    }

    @Override
    public ProductoResponseDTO cambiarDisponibilidad(Long id, Boolean disponible) {
        log.info("Cambiando disponibilidad del producto {} a disponible={}", id, disponible);

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

        if (!producto.getActivo() && disponible) {
            throw new BusinessException("No se puede disponibilizar un producto inactivo");
        }

        producto.setDisponible(disponible);
        Producto actualizado = productoRepository.save(producto);

        return productoMapper.toResponseDTO(actualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public Double obtenerPrecioProducto(Long id) {
        log.debug("Obteniendo precio del producto ID: {}", id);

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

        if (!producto.getActivo()) {
            throw new BusinessException("No se puede obtener precio de un producto inactivo");
        }

        if (producto.getPrecio() == null || producto.getPrecio() <= 0) {
            throw new BusinessException("El producto tiene un precio inválido");
        }

        return producto.getPrecio();
    }

    @Override
    public boolean existsById(Long id) {
        return productoRepository.existsById(id);
    }
}