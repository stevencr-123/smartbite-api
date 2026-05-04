package com.smartbite.administrativo.mapper;

import com.smartbite.administrativo.dto.CategoriaRequestDTO;
import com.smartbite.administrativo.dto.CategoriaResponseDTO;
import com.smartbite.administrativo.model.Categoria;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CategoriaMapper {

    CategoriaMapper INSTANCE = Mappers.getMapper(CategoriaMapper.class);

    // RequestDTO → Entity (ignorando la relación)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "subcategorias", ignore = true)
    @Mapping(target = "categoriaPadre", ignore = true)  // ← Se asigna manualmente en el Service
    Categoria toEntity(CategoriaRequestDTO requestDTO);

    // Entity → ResponseDTO
    @Mapping(target = "categoriaPadreId", source = "categoriaPadre.id")
    @Mapping(target = "categoriaPadreNombre", source = "categoriaPadre.nombre")
    CategoriaResponseDTO toResponseDTO(Categoria categoria);
}