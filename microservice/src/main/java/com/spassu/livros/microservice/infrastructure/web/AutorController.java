package com.spassu.livros.microservice.infrastructure.web;

import com.spassu.livros.microservice.application.dto.AutorRequest;
import com.spassu.livros.microservice.application.dto.AutorResponse;
import com.spassu.livros.microservice.application.usecase.AutorUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Autores", description = "CRUD de Autores")
public class AutorController {

    private final AutorUseCase useCase;

    @GetMapping
    @Operation(summary = "Listar autores paginados")
    public Page<AutorResponse> listar(@PageableDefault(size = 20) Pageable pageable) {
        return useCase.listar(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar autor por ID")
    public AutorResponse buscar(@PathVariable Integer id) {
        return useCase.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar autor")
    public AutorResponse criar(@Valid @RequestBody AutorRequest request) {
        return useCase.criar(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar autor")
    public AutorResponse atualizar(@PathVariable Integer id,
                                   @Valid @RequestBody AutorRequest request) {
        return useCase.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir autor")
    public void excluir(@PathVariable Integer id) {
        useCase.excluir(id);
    }
}
