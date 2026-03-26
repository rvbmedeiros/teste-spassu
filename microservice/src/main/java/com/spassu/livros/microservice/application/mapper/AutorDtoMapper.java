package com.spassu.livros.microservice.application.mapper;

import com.spassu.livros.microservice.application.dto.AutorRequest;
import com.spassu.livros.microservice.application.dto.AutorResponse;
import com.spassu.livros.microservice.domain.model.Autor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface AutorDtoMapper {

    AutorResponse toResponse(Autor autor);

    @Mapping(target = "codAu", ignore = true)
    Autor toDomain(AutorRequest request);

    @Mapping(target = "codAu", ignore = true)
    void updateDomain(@MappingTarget Autor autor, AutorRequest request);

    default Page<AutorResponse> toResponsePage(Page<Autor> page) {
        return page.map(this::toResponse);
    }
}
