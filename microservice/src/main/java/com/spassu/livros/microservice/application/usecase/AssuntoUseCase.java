package com.spassu.livros.microservice.application.usecase;

import com.spassu.livros.microservice.application.dto.AssuntoRequest;
import com.spassu.livros.microservice.application.dto.AssuntoResponse;
import com.spassu.livros.microservice.application.mapper.AssuntoDtoMapper;
import com.spassu.livros.microservice.domain.exception.EntityNotFoundException;
import com.spassu.livros.microservice.domain.model.Assunto;
import com.spassu.livros.microservice.domain.repository.AssuntoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AssuntoUseCase {

    private final AssuntoRepository repository;
    private final AssuntoDtoMapper  mapper;

    public AssuntoResponse criar(AssuntoRequest request) {
        Assunto assunto = mapper.toDomain(request);
        return mapper.toResponse(repository.save(assunto));
    }

    public AssuntoResponse atualizar(Integer id, AssuntoRequest request) {
        Assunto assunto = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assunto", id));
        mapper.updateDomain(assunto, request);
        return mapper.toResponse(repository.save(assunto));
    }

    public AssuntoResponse buscarPorId(Integer id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Assunto", id));
    }

    public Page<AssuntoResponse> listar(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    public void excluir(Integer id) {
        repository.deleteById(id);
    }
}
