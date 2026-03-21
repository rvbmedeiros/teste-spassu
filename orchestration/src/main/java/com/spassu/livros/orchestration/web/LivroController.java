package com.spassu.livros.orchestration.web;

import com.spassu.livros.orchestration.dto.LivroRequest;
import com.spassu.livros.orchestration.dto.LivroResponse;
import com.spassu.livros.orchestration.flow.CreateLivroFlow;
import com.spassu.livros.orchestration.flow.DeleteLivroFlow;
import com.spassu.livros.orchestration.flow.UpdateLivroFlow;
import com.spassu.livros.orchestration.client.MicroserviceClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/livros")
@Tag(name = "Livros", description = "CRUD de Livros com orquestração FlowCockpit")
public class LivroController {

    private final CreateLivroFlow createFlow;
    private final UpdateLivroFlow updateFlow;
    private final DeleteLivroFlow deleteFlow;
    private final MicroserviceClient client;

    public LivroController(CreateLivroFlow createFlow, UpdateLivroFlow updateFlow,
                            DeleteLivroFlow deleteFlow, MicroserviceClient client) {
        this.createFlow = createFlow;
        this.updateFlow = updateFlow;
        this.deleteFlow = deleteFlow;
        this.client = client;
    }

    @GetMapping
    @Operation(summary = "Listar livros")
    public Mono<List<LivroResponse>> listar(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return client.listarLivros(page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar livro por ID")
    public Mono<LivroResponse> buscarPorId(@PathVariable Integer id) {
        return client.buscarLivroPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar livro")
    public Mono<LivroResponse> criar(@RequestBody @Valid LivroRequest request) {
        return createFlow.execute(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar livro")
    public Mono<LivroResponse> atualizar(
            @PathVariable Integer id,
            @RequestBody @Valid LivroRequest request) {
        return updateFlow.execute(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir livro")
    public Mono<Void> excluir(@PathVariable Integer id) {
        return deleteFlow.execute(id);
    }
}
