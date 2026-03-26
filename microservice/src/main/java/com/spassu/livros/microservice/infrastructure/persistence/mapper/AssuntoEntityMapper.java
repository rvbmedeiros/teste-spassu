package com.spassu.livros.microservice.infrastructure.persistence.mapper;

import com.spassu.livros.microservice.domain.model.Assunto;
import com.spassu.livros.microservice.infrastructure.persistence.entity.AssuntoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.BeanMapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface AssuntoEntityMapper {

    Assunto toDomain(AssuntoEntity entity);

    AssuntoEntity toEntity(Assunto domain);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget AssuntoEntity entity, Assunto domain);
}
