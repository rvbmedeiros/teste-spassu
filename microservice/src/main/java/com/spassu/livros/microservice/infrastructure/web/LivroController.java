package com.spassu.livros.microservice.infrastructure.web;

import com.spassu.livros.microservice.application.dto.LivroRequest;
import com.spassu.livros.microservice.application.dto.LivroResponse;
import com.spassu.livros.microservice.application.usecase.LivroUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/livros")
@RequiredArgsConstructor
public class LivroController {

    private final LivroUseCase useCase;

    @GetMapping
    public Page<LivroResponse> listar(@PageableDefault(size = 20) Pageable pageable) {
        return useCase.listar(pageable);
    }

    @GetMapping("/{id}")
    public LivroResponse buscar(@PathVariable Integer id) {
        return useCase.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LivroResponse criar(@Valid @RequestBody LivroRequest request) {
        return useCase.criar(request);
    }

    @PutMapping("/{id}")
    public LivroResponse atualizar(@PathVariable Integer id,
                                   @Valid @RequestBody LivroRequest request) {
        return useCase.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Integer id) {
        useCase.excluir(id);
    }
}
