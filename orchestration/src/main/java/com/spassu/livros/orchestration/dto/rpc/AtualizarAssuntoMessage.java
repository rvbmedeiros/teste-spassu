package com.spassu.livros.orchestration.dto.rpc;

import com.spassu.livros.orchestration.dto.AssuntoRequest;

public record AtualizarAssuntoMessage(Integer id, AssuntoRequest request) {}
