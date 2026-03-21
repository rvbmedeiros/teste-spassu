package com.spassu.livros.microservice.application.dto.rpc;

public record RpcResponse<T>(
        boolean success,
        T payload,
        String errorCode,
        String message,
        int httpStatus
) {
    public static <T> RpcResponse<T> ok(T payload, int httpStatus) {
        return new RpcResponse<>(true, payload, null, null, httpStatus);
    }

    public static <T> RpcResponse<T> error(String errorCode, String message, int httpStatus) {
        return new RpcResponse<>(false, null, errorCode, message, httpStatus);
    }
}
