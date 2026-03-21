package com.spassu.livros.orchestration.web;

import com.spassu.livros.orchestration.flow.GetRelatorioFlow;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/relatorio")
@Tag(name = "Relatório")
public class RelatorioController {

    private final GetRelatorioFlow flow;

    public RelatorioController(GetRelatorioFlow flow) {
        this.flow = flow;
    }

    @GetMapping("/pdf")
    @Operation(summary = "Gerar relatório PDF de livros por autor")
    public Mono<ResponseEntity<byte[]>> gerarPdf() {
        return flow.execute()
                .map(bytes -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"relatorio-livros.pdf\"")
                        .body(bytes));
    }
}
