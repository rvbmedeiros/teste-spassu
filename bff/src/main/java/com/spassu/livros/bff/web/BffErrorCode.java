package com.spassu.livros.bff.web;

public enum BffErrorCode {
    UNAUTHORIZED("BFF_0001"),
    FORBIDDEN("BFF_0002"),
    ORCHESTRATION_BAD_REQUEST("BFF_0800"),
    ORCHESTRATION_NOT_FOUND("BFF_0804"),
    ORCHESTRATION_UNAVAILABLE("BFF_1002"),
    ORCHESTRATION_INTERNAL("BFF_1003"),
    INTERNAL_ERROR("BFF_9000");

    private final String code;

    BffErrorCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
