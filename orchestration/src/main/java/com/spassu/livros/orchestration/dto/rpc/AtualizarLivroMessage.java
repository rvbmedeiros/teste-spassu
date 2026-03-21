package com.spassu.livros.orchestration.dto.rpc;

import com.spassu.livros.orchestration.dto.LivroRequest;

public record AtualizarLivroMessage(Integer id, LivroRequest request) {}
