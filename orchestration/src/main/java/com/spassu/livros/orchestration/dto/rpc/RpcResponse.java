package com.spassu.livros.orchestration.dto.rpc;

public record RpcResponse<T>(
        boolean success,
        T payload,
        String errorCode,
        String message,
        int httpStatus
) {}
