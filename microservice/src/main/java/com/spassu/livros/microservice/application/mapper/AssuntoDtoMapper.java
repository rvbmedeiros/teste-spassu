package com.spassu.livros.microservice.application.mapper;

import com.spassu.livros.microservice.application.dto.AssuntoRequest;
import com.spassu.livros.microservice.application.dto.AssuntoResponse;
import com.spassu.livros.microservice.domain.model.Assunto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface AssuntoDtoMapper {

    AssuntoResponse toResponse(Assunto assunto);

    @Mapping(target = "codAs", ignore = true)
    Assunto toDomain(AssuntoRequest request);

    @Mapping(target = "codAs", ignore = true)
    void updateDomain(@MappingTarget Assunto assunto, AssuntoRequest request);

    default Page<AssuntoResponse> toResponsePage(Page<Assunto> page) {
        return page.map(this::toResponse);
    }
}
