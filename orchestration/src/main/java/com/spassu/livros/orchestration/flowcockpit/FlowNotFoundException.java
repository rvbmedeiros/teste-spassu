package com.spassu.livros.orchestration.flowcockpit;

public class FlowNotFoundException extends RuntimeException {

    public FlowNotFoundException(String id) {
        super("Flow not found: " + id);
    }
}
