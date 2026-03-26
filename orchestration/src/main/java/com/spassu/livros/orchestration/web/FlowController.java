package com.spassu.livros.orchestration.web;

import com.spassu.livros.orchestration.flowcockpit.FlowGraph;
import com.spassu.livros.orchestration.flowcockpit.FlowNarrative;
import com.spassu.livros.orchestration.flowcockpit.FlowRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/flows")
@Tag(name = "FlowCockpit", description = "Estrutura estática dos fluxos de orquestração")
public class FlowController {

    private final FlowRegistry registry;

    public FlowController(FlowRegistry registry) {
        this.registry = registry;
    }

    @GetMapping
    @Operation(summary = "Listar todos os fluxos registrados (estrutura estática)")
    public List<FlowGraph> listar() {
        return registry.getAllFlows();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar fluxo por ID")
    public FlowGraph buscarPorId(@PathVariable String id) {
        return registry.findById(id);
    }

    @GetMapping("/{id}/narrative")
    @Operation(summary = "Gerar narrativa textual do fluxo por ID")
    public FlowNarrative narrativa(@PathVariable String id) {
        return registry.narrativeById(id);
    }
}
