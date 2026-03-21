package com.spassu.livros.microservice.application.dto.rpc;

import com.spassu.livros.microservice.application.dto.AssuntoRequest;

public record AtualizarAssuntoMessage(Integer id, AssuntoRequest request) {}
