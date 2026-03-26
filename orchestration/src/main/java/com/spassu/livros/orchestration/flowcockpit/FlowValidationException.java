package com.spassu.livros.orchestration.flowcockpit;

/**
 * Raised when a flow definition produces an invalid structural graph.
 */
public class FlowValidationException extends RuntimeException {

    public FlowValidationException(String message) {
        super(message);
    }
}
