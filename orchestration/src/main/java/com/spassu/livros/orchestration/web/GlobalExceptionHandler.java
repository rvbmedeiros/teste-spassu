package com.spassu.livros.orchestration.web;

import com.spassu.livros.orchestration.client.MicroserviceRpcException;
import com.spassu.livros.orchestration.flowcockpit.FlowNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

import java.net.URI;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FlowNotFoundException.class)
    public ProblemDetail handleFlowNotFound(FlowNotFoundException ex) {
        log.error("Flow not found. reason={}", ex.getMessage(), ex);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setType(URI.create("https://spassu.com/problems/flow-not-found"));
        return pd;
    }

    @ExceptionHandler(MicroserviceRpcException.class)
    public ProblemDetail handleRpcError(MicroserviceRpcException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getHttpStatus());
        if (status == null) {
            status = HttpStatus.BAD_GATEWAY;
        }
        log.error("RPC microservice error. status={}, code={}, detail={}",
                status.value(),
                ex.getErrorCode(),
                ex.getMessage(),
                ex);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        pd.setType(URI.create("https://spassu.com/problems/ms-rpc-error"));
        pd.setProperty("errorCode", ex.getErrorCode());
        return pd;
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoResourceFound(NoResourceFoundException ex) {
        log.warn("No resource found for request path. message={}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setType(URI.create("https://spassu.com/problems/not-found"));
        return pd;
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ProblemDetail handleValidationError(WebExchangeBindException ex) {
        log.warn("Request validation failed. message={}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, "Validation failed");
        pd.setType(URI.create("https://spassu.com/problems/validation-error"));
        Map<String, String> fieldErrors = ex.getFieldErrors().stream()
                .collect(java.util.stream.Collectors.toMap(
                        fieldError -> fieldError.getField(),
                        fieldError -> fieldError.getDefaultMessage() == null ? "invalid value" : fieldError.getDefaultMessage(),
                        (first, second) -> first
                ));
        pd.setProperty("errors", fieldErrors);
        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        log.error("Unhandled orchestration error.", ex);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
        pd.setType(URI.create("https://spassu.com/problems/internal-error"));
        return pd;
    }
}
