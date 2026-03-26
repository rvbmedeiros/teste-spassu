package com.spassu.livros.orchestration.flowcockpit;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Enforces structural integrity rules for FlowCockpit graphs.
 */
final class FlowGraphValidator {

    void validate(String flowId, List<FlowGraph.FlowNode> nodes, List<FlowGraph.FlowEdge> edges) {
        Map<String, FlowGraph.FlowNode> nodeById = nodes.stream()
                .collect(Collectors.toMap(FlowGraph.FlowNode::nodeId, Function.identity()));

        for (FlowGraph.FlowEdge edge : edges) {
            if (edge.from() == null || edge.from().isBlank()) {
                throw new FlowValidationException("flow=" + flowId + " has edge with blank 'from' node");
            }
            if (edge.to() == null || edge.to().isBlank()) {
                throw new FlowValidationException("flow=" + flowId + " has edge from='" + edge.from() + "' with blank 'to' node");
            }
            if (!nodeById.containsKey(edge.from())) {
                throw new FlowValidationException("flow=" + flowId + " has edge from unknown node='" + edge.from() + "'");
            }
            if (!nodeById.containsKey(edge.to())) {
                throw new FlowValidationException("flow=" + flowId + " has edge to unknown node='" + edge.to() + "'");
            }
        }

        for (FlowGraph.FlowNode node : nodes) {
            if (node.type() != NodeType.EXCLUSIVE_GATEWAY && node.type() != NodeType.PARALLEL_GATEWAY) {
                continue;
            }

            List<FlowGraph.FlowEdge> outgoing = edges.stream()
                    .filter(edge -> node.nodeId().equals(edge.from()))
                    .toList();

            if (node.type() == NodeType.EXCLUSIVE_GATEWAY && outgoing.size() != 2) {
                throw new FlowValidationException(
                        "flow=" + flowId + " gateway='" + node.nodeId() + "' type=" + node.type() + " must define exactly 2 branches"
                );
            }

            if (node.type() == NodeType.PARALLEL_GATEWAY && outgoing.isEmpty()) {
                throw new FlowValidationException(
                        "flow=" + flowId + " gateway='" + node.nodeId() + "' type=" + node.type() + " must define at least 1 branch"
                );
            }

            java.util.Set<String> exclusiveIntents = new java.util.HashSet<>();
            for (int i = 0; i < outgoing.size(); i++) {
                FlowGraph.FlowEdge branch = outgoing.get(i);
                if (branch.label() == null || branch.label().isBlank()) {
                    throw new FlowValidationException(
                            "flow=" + flowId + " gateway='" + node.nodeId() + "' branchIndex=" + i + " must define a non-blank label"
                    );
                }

                if (node.type() == NodeType.EXCLUSIVE_GATEWAY) {
                    if (branch.edgeIntent() == null || branch.edgeIntent().isBlank()) {
                        throw new FlowValidationException(
                                "flow=" + flowId + " gateway='" + node.nodeId() + "' branchIndex=" + i + " must define a non-blank edgeIntent"
                        );
                    }
                    if (!exclusiveIntents.add(branch.edgeIntent())) {
                        throw new FlowValidationException(
                                "flow=" + flowId + " gateway='" + node.nodeId() + "' has duplicated edgeIntent='" + branch.edgeIntent() + "'"
                        );
                    }
                }
            }
        }
    }
}
