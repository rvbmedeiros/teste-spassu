package com.spassu.livros.orchestration.flowcockpit;

import java.util.List;

/**
 * Human-readable narrative derived from the static flow graph.
 */
public record FlowNarrative(
        String flowId,
        String flowName,
        String businessGoal,
        List<String> paths
) {
}
