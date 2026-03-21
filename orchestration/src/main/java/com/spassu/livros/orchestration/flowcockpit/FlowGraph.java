package com.spassu.livros.orchestration.flowcockpit;

import java.util.List;

/**
 * Static graph of a flow, built once at startup by {@link FlowRegistry}.
 * Returned by GET /flows for the FlowCockpit canvas to render.
 */
public record FlowGraph(
        String id,
        String name,
        String description,
        List<FlowNode> steps
) {

    public record FlowNode(
            int order,
            String name,
            String description,
            String rollbackStep
    ) {}
}
