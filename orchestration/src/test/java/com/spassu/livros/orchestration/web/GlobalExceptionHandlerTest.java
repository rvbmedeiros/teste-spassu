package com.spassu.livros.orchestration.web;

import com.spassu.livros.orchestration.client.MicroserviceRpcException;
import com.spassu.livros.orchestration.flowcockpit.FlowNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.lang.reflect.Method;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("handleFlowNotFound deve retornar problem detail 404")
    void handleFlowNotFound_deveRetornarProblemDetail404() {
        var result = handler.handleFlowNotFound(new FlowNotFoundException("fluxo nao encontrado"));

        assertThat(result.getStatus()).isEqualTo(404);
        assertThat(result.getDetail()).isEqualTo("Flow not found: fluxo nao encontrado");
        assertThat(result.getType().toString()).isEqualTo("https://spassu.com/problems/flow-not-found");
    }

    @Test
    @DisplayName("handleRpcError deve preservar status e codigo")
    void handleRpcError_devePreservarStatusECodigo() {
        var exception = new MicroserviceRpcException("RPC_001", "falha remota", 422);

        var result = handler.handleRpcError(exception);

        assertThat(result.getStatus()).isEqualTo(422);
        assertThat(result.getDetail()).isEqualTo("falha remota");
        assertThat(result.getType().toString()).isEqualTo("https://spassu.com/problems/ms-rpc-error");
        assertThat(result.getProperties()).containsEntry("errorCode", "RPC_001");
    }

    @Test
    @DisplayName("handleRpcError deve usar bad gateway quando status for invalido")
    void handleRpcError_quandoStatusInvalido_deveUsarBadGateway() {
        var exception = new MicroserviceRpcException("RPC_002", "falha remota", 999);

        var result = handler.handleRpcError(exception);

        assertThat(result.getStatus()).isEqualTo(502);
    }

    @Test
    @DisplayName("handleGeneric deve retornar problem detail 500")
    void handleGeneric_deveRetornarProblemDetail500() {
        var result = handler.handleGeneric(new RuntimeException("erro"));

        assertThat(result.getStatus()).isEqualTo(500);
        assertThat(result.getDetail()).isEqualTo("Internal server error");
        assertThat(result.getType().toString()).isEqualTo("https://spassu.com/problems/internal-error");
    }

    @Test
    @DisplayName("handleValidationError deve retornar problem detail 422 com erros de campo")
    void handleValidationError_deveRetornarProblemDetail422ComErrosDeCampo() throws Exception {
        Method method = ValidationStub.class.getDeclaredMethod("handle", String.class);
        MethodParameter methodParameter = new MethodParameter(method, 0);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "livroRequest");
        bindingResult.addError(new FieldError("livroRequest", "anoPublicacao", "deve corresponder a \"\\d{4}\""));
        WebExchangeBindException exception = new WebExchangeBindException(methodParameter, bindingResult);

        var result = handler.handleValidationError(exception);

        assertThat(result.getStatus()).isEqualTo(422);
        assertThat(result.getDetail()).isEqualTo("Validation failed");
        assertThat(result.getType().toString()).isEqualTo("https://spassu.com/problems/validation-error");
        assertThat(result.getProperties()).containsKey("errors");
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) result.getProperties().get("errors");
        assertThat(errors)
                .containsEntry("anoPublicacao", "deve corresponder a \"\\d{4}\"");
    }

    static class ValidationStub {
        @SuppressWarnings("unused")
        void handle(String payload) {
            // no-op for MethodParameter construction in tests
        }
    }
}