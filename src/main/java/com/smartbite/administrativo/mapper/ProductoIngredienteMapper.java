package com.smartbite.administrativo.mapper;

import com.smartbite.administrativo.dto.ProductoIngredienteRequestDTO;
import com.smartbite.administrativo.dto.ProductoIngredienteResponseDTO;
import com.smartbite.administrativo.model.ProductoIngrediente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ProductoIngredienteMapper {

    ProductoIngredienteMapper INSTANCE = Mappers.getMapper(ProductoIngredienteMapper.class);

    // RequestDTO → Entity (ignorando relaciones)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "producto", ignore = true)        // ← Se asigna manualmente
    @Mapping(target = "itemInventario", ignore = true)  // ← Se asigna manualmente
    ProductoIngrediente toEntity(ProductoIngredienteRequestDTO requestDTO);

    // Entity → ResponseDTO
    @Mapping(target = "productoId", source = "producto.id")
    @Mapping(target = "productoNombre", source = "producto.nombre")
    @Mapping(target = "productoPrecio", source = "producto.precio")
    @Mapping(target = "itemInventarioId", source = "itemInventario.id")
    @Mapping(target = "itemInventarioNombre", source = "itemInventario.nombre")
    @Mapping(target = "itemInventarioStockActual", source = "itemInventario.stockActual")
    @Mapping(target = "itemInventarioUnidadMedida", source = "itemInventario.unidadMedida")
    ProductoIngredienteResponseDTO toResponseDTO(ProductoIngrediente productoIngrediente);
}