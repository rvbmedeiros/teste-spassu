package com.spassu.livros.orchestration.dto.rpc;

import com.spassu.livros.orchestration.dto.AutorRequest;

public record AtualizarAutorMessage(Integer id, AutorRequest request) {}
