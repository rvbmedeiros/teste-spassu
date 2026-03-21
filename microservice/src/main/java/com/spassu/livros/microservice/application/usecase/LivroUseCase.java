package com.spassu.livros.microservice.application.usecase;

import com.spassu.livros.microservice.application.dto.LivroRequest;
import com.spassu.livros.microservice.application.dto.LivroResponse;
import com.spassu.livros.microservice.application.mapper.LivroDtoMapper;
import com.spassu.livros.microservice.domain.exception.EntityNotFoundException;
import com.spassu.livros.microservice.domain.model.Autor;
import com.spassu.livros.microservice.domain.model.Assunto;
import com.spassu.livros.microservice.domain.model.Livro;
import com.spassu.livros.microservice.domain.repository.AutorRepository;
import com.spassu.livros.microservice.domain.repository.AssuntoRepository;
import com.spassu.livros.microservice.domain.repository.LivroRepository;
import com.spassu.livros.microservice.infrastructure.messaging.LivroEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class LivroUseCase {

    private final LivroRepository       repository;
    private final AutorRepository       autorRepository;
    private final AssuntoRepository     assuntoRepository;
    private final LivroDtoMapper        mapper;
    private final LivroEventPublisher   eventPublisher;

    public LivroResponse criar(LivroRequest request) {
        Livro livro = mapper.toDomain(request);
        resolveAssociations(livro, request);
        Livro salvo = repository.save(livro);
        eventPublisher.publishCreated(salvo);
        return mapper.toResponse(salvo);
    }

    public LivroResponse atualizar(Integer id, LivroRequest request) {
        Livro livro = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Livro", id));
        livro.setTitulo(request.getTitulo());
        livro.setEditora(request.getEditora());
        livro.setEdicao(request.getEdicao());
        livro.setAnoPublicacao(request.getAnoPublicacao());
        livro.setValor(request.getValor());
        resolveAssociations(livro, request);
        Livro salvo = repository.save(livro);
        eventPublisher.publishUpdated(salvo);
        return mapper.toResponse(salvo);
    }

    public LivroResponse buscarPorId(Integer id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Livro", id));
    }

    public Page<LivroResponse> listar(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    public void excluir(Integer id) {
        repository.deleteById(id);
        eventPublisher.publishDeleted(id);
    }

    private void resolveAssociations(Livro livro, LivroRequest request) {
        Set<Autor>   autores  = autorRepository.findAllByIds(request.getAutoresCodAu());
        Set<Assunto> assuntos = assuntoRepository.findAllByIds(request.getAssuntosCodAs());

        // clear and replace via domain methods
        livro.getAutores().clear();
        livro.getAssuntos().clear();
        autores.forEach(livro::adicionarAutor);
        assuntos.forEach(livro::adicionarAssunto);
    }
}
