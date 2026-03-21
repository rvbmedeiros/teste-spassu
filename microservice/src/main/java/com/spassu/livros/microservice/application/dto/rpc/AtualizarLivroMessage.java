package com.spassu.livros.microservice.application.dto.rpc;

import com.spassu.livros.microservice.application.dto.LivroRequest;

public record AtualizarLivroMessage(Integer id, LivroRequest request) {}
