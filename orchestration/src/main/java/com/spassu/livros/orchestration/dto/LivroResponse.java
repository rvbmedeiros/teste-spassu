package com.spassu.livros.orchestration.dto;

import java.math.BigDecimal;
import java.util.List;

public record LivroResponse(
        Integer codL,
        String titulo,
        String editora,
        Integer edicao,
        String anoPublicacao,
        BigDecimal valor,
        List<AutorResponse> autores,
        List<AssuntoResponse> assuntos
) {}
