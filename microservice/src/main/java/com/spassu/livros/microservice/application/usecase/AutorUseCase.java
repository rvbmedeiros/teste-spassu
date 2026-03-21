package com.spassu.livros.microservice.application.usecase;

import com.spassu.livros.microservice.application.dto.AutorRequest;
import com.spassu.livros.microservice.application.dto.AutorResponse;
import com.spassu.livros.microservice.application.mapper.AutorDtoMapper;
import com.spassu.livros.microservice.domain.exception.EntityNotFoundException;
import com.spassu.livros.microservice.domain.model.Autor;
import com.spassu.livros.microservice.domain.repository.AutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AutorUseCase {

    private final AutorRepository  repository;
    private final AutorDtoMapper   mapper;

    public AutorResponse criar(AutorRequest request) {
        Autor autor = mapper.toDomain(request);
        return mapper.toResponse(repository.save(autor));
    }

    public AutorResponse atualizar(Integer id, AutorRequest request) {
        Autor autor = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Autor", id));
        mapper.updateDomain(autor, request);
        return mapper.toResponse(repository.save(autor));
    }

    public AutorResponse buscarPorId(Integer id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Autor", id));
    }

    public Page<AutorResponse> listar(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    public void excluir(Integer id) {
        repository.deleteById(id);
    }
}
