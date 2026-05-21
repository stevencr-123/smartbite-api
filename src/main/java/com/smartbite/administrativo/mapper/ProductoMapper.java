package com.smartbite.administrativo.mapper;

import com.smartbite.administrativo.dto.ProductoRequestDTO;
import com.smartbite.administrativo.dto.ProductoResponseDTO;
import com.smartbite.administrativo.model.Producto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ProductoMapper {

    ProductoMapper INSTANCE = Mappers.getMapper(ProductoMapper.class);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "disponible", ignore = true)
    @Mapping(target = "categoria", ignore = true)   // ← Se asigna manualmente
    @Mapping(target = "sucursal", ignore = true)    // ← Se asigna manualmente
    Producto toEntity(ProductoRequestDTO requestDTO);


    @Mapping(target = "categoriaId", source = "categoria.id")
    @Mapping(target = "categoriaNombre", source = "categoria.nombre")
    @Mapping(target = "sucursalId", source = "sucursal.id")
    @Mapping(target = "sucursalNombre", source = "sucursal.nombre")
    ProductoResponseDTO toResponseDTO(Producto producto);
}