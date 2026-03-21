package com.spassu.livros.microservice.application.mapper;

import com.spassu.livros.microservice.application.dto.LivroRequest;
import com.spassu.livros.microservice.application.dto.LivroResponse;
import com.spassu.livros.microservice.domain.model.Livro;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring", uses = {AutorDtoMapper.class, AssuntoDtoMapper.class})
public interface LivroDtoMapper {

    LivroResponse toResponse(Livro livro);

    /**
     * Only maps scalar fields — autores/assuntos are resolved by the use case
     * before calling the domain repository.
     */
    @Mapping(target = "codL",     ignore = true)
    @Mapping(target = "autores",  ignore = true)
    @Mapping(target = "assuntos", ignore = true)
    Livro toDomain(LivroRequest request);

    default Page<LivroResponse> toResponsePage(Page<Livro> page) {
        return page.map(this::toResponse);
    }
}
