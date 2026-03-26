package com.spassu.livros.microservice.infrastructure.web;

import com.spassu.livros.microservice.application.dto.AutorRequest;
import com.spassu.livros.microservice.application.dto.AutorResponse;
import com.spassu.livros.microservice.application.usecase.AutorUseCase;
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
@RequestMapping("/api/autores")
@RequiredArgsConstructor
public class AutorController {

    private final AutorUseCase useCase;

    @GetMapping
    public Page<AutorResponse> listar(@PageableDefault(size = 20) Pageable pageable) {
        return useCase.listar(pageable);
    }

    @GetMapping("/{id}")
    public AutorResponse buscar(@PathVariable Integer id) {
        return useCase.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AutorResponse criar(@Valid @RequestBody AutorRequest request) {
        return useCase.criar(request);
    }

    @PutMapping("/{id}")
    public AutorResponse atualizar(@PathVariable Integer id,
                                   @Valid @RequestBody AutorRequest request) {
        return useCase.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Integer id) {
        useCase.excluir(id);
    }
}
