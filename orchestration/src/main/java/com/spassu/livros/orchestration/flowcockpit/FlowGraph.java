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
        String owner,
        String version,
        String domainTag,
        String businessGoal,
        List<FlowNode> nodes,
        List<FlowEdge> edges
) {

    public record FlowNode(
            String nodeId,
            NodeType type,
            int order,
            String name,
            String description,
            String purpose,
            String inputHint,
            String outputHint,
            String failureHint,
            String rollbackStep
    ) {}

    public record FlowEdge(
            String from,
            String to,
            String label,
            String edgeIntent
    ) {}
}
