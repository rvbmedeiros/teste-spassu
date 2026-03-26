package com.spassu.livros.orchestration.web;

import com.spassu.livros.orchestration.client.MicroserviceClient;
import com.spassu.livros.orchestration.dto.AssuntoRequest;
import com.spassu.livros.orchestration.dto.AssuntoResponse;
import com.spassu.livros.orchestration.flow.CreateAssuntoFlow;
import com.spassu.livros.orchestration.flow.DeleteAssuntoFlow;
import com.spassu.livros.orchestration.flow.UpdateAssuntoFlow;
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
@RequestMapping("/api/assuntos")
@Tag(name = "Assuntos")
public class AssuntoController {

    private final CreateAssuntoFlow createFlow;
    private final UpdateAssuntoFlow updateFlow;
    private final DeleteAssuntoFlow deleteFlow;
    private final MicroserviceClient client;

    public AssuntoController(
            CreateAssuntoFlow createFlow,
            UpdateAssuntoFlow updateFlow,
            DeleteAssuntoFlow deleteFlow,
            MicroserviceClient client) {
        this.createFlow = createFlow;
        this.updateFlow = updateFlow;
        this.deleteFlow = deleteFlow;
        this.client = client;
    }

    @GetMapping
    @Operation(summary = "Listar assuntos")
    public Mono<List<AssuntoResponse>> listar() {
        return client.listarAssuntos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar assunto por ID")
    public Mono<AssuntoResponse> buscarPorId(@PathVariable Integer id) {
        return client.buscarAssuntoPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar assunto")
    public Mono<AssuntoResponse> criar(@RequestBody @Valid AssuntoRequest request) {
        return createFlow.execute(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar assunto")
    public Mono<AssuntoResponse> atualizar(
            @PathVariable Integer id,
            @RequestBody @Valid AssuntoRequest request) {
        return updateFlow.execute(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir assunto")
    public Mono<Void> excluir(@PathVariable Integer id) {
        return deleteFlow.execute(id);
    }
}
