package com.spassu.livros.microservice.infrastructure.web;

import com.spassu.livros.microservice.application.usecase.RelatorioUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/relatorio")
@RequiredArgsConstructor
@Tag(name = "Relatório", description = "Relatório de livros por autor em PDF")
public class RelatorioController {

    private final RelatorioUseCase useCase;

    @GetMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Gerar relatório de livros agrupados por autor em PDF")
    public ResponseEntity<byte[]> gerarPdf() {
        byte[] pdf = useCase.gerarPdf();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"relatorio-livros.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
