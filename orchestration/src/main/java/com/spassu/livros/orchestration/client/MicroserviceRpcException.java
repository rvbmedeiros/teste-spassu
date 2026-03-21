package com.spassu.livros.orchestration.client;

import lombok.Getter;

@Getter
public class MicroserviceRpcException extends RuntimeException {

    private final String errorCode;
    private final int httpStatus;

    public MicroserviceRpcException(String errorCode, String message, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
