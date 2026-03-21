package com.spassu.livros.microservice.application.dto.rpc;

import com.spassu.livros.microservice.application.dto.AutorRequest;

public record AtualizarAutorMessage(Integer id, AutorRequest request) {}
