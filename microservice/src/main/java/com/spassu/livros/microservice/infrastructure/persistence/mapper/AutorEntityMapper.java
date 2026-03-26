package com.spassu.livros.microservice.infrastructure.persistence.mapper;

import com.spassu.livros.microservice.domain.model.Autor;
import com.spassu.livros.microservice.infrastructure.persistence.entity.AutorEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.BeanMapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface AutorEntityMapper {

    Autor toDomain(AutorEntity entity);

    AutorEntity toEntity(Autor domain);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget AutorEntity entity, Autor domain);
}
