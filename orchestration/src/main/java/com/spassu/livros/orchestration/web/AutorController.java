package com.spassu.livros.orchestration.web;

import com.spassu.livros.orchestration.client.MicroserviceClient;
import com.spassu.livros.orchestration.dto.AutorRequest;
import com.spassu.livros.orchestration.dto.AutorResponse;
import com.spassu.livros.orchestration.flow.CreateAutorFlow;
import com.spassu.livros.orchestration.flow.DeleteAutorFlow;
import com.spassu.livros.orchestration.flow.UpdateAutorFlow;
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
@RequestMapping("/api/autores")
@Tag(name = "Autores")
public class AutorController {

    private final CreateAutorFlow createFlow;
    private final UpdateAutorFlow updateFlow;
    private final DeleteAutorFlow deleteFlow;
    private final MicroserviceClient client;

    public AutorController(
            CreateAutorFlow createFlow,
            UpdateAutorFlow updateFlow,
            DeleteAutorFlow deleteFlow,
            MicroserviceClient client) {
        this.createFlow = createFlow;
        this.updateFlow = updateFlow;
        this.deleteFlow = deleteFlow;
        this.client = client;
    }

    @GetMapping
    @Operation(summary = "Listar autores")
    public Mono<List<AutorResponse>> listar() {
        return client.listarAutores();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar autor por ID")
    public Mono<AutorResponse> buscarPorId(@PathVariable Integer id) {
        return client.buscarAutorPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar autor")
    public Mono<AutorResponse> criar(@RequestBody @Valid AutorRequest request) {
        return createFlow.execute(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar autor")
    public Mono<AutorResponse> atualizar(
            @PathVariable Integer id,
            @RequestBody @Valid AutorRequest request) {
        return updateFlow.execute(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir autor")
    public Mono<Void> excluir(@PathVariable Integer id) {
        return deleteFlow.execute(id);
    }
}
