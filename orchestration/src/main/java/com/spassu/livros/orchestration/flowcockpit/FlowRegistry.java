package com.spassu.livros.orchestration.flowcockpit;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Scans the Spring context at startup for all beans annotated with
 * {@link FlowDefinition} and builds immutable BPM-like graphs for FlowCockpit.
 */
@Slf4j
@Component
public class FlowRegistry implements ApplicationContextAware {

    private ApplicationContext ctx;
    private List<FlowGraph> graphs = List.of();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.ctx = applicationContext;
    }

    @PostConstruct
    void buildRegistry() {
        Map<String, Object> beans = ctx.getBeansWithAnnotation(FlowDefinition.class);
        List<FlowGraph> result = new ArrayList<>(beans.size());

        for (Object bean : beans.values()) {
            Class<?> target = resolveTargetClass(bean);
            FlowDefinition def = target.getAnnotation(FlowDefinition.class);
            if (def == null) {
                continue;
            }

            List<StepSpec> steps = readStepSpecs(target);
            FlowStartEvent startEvent = target.getAnnotation(FlowStartEvent.class);
            FlowGateway[] gateways = target.getAnnotationsByType(FlowGateway.class);
            FlowEndEvent[] endEvents = target.getAnnotationsByType(FlowEndEvent.class);

            boolean bpmMode = startEvent != null || gateways.length > 0 || endEvents.length > 0;
            FlowGraph graph = bpmMode
                    ? buildBpmGraph(def, steps, startEvent, gateways, endEvents)
                    : buildLegacyGraph(def, steps);
            result.add(graph);
        }

        this.graphs = List.copyOf(result);
    }

    private Class<?> resolveTargetClass(Object bean) {
        Class<?> beanClass = bean.getClass();
        Class<?> superClass = beanClass.getSuperclass();
        if (superClass != null && superClass.isAnnotationPresent(FlowDefinition.class)) {
            return superClass;
        }
        return beanClass;
    }

    private List<StepSpec> readStepSpecs(Class<?> target) {
        List<StepSpec> specs = new ArrayList<>();
        for (Method method : target.getDeclaredMethods()) {
            FlowStep step = method.getAnnotation(FlowStep.class);
            if (step == null) {
                continue;
            }

            String nodeId = step.nodeId().isBlank() ? "step-" + step.order() : step.nodeId();
            specs.add(new StepSpec(nodeId, step));
        }

        specs.sort(Comparator.comparingInt(spec -> spec.step.order()));
        return specs;
    }

    private FlowGraph buildLegacyGraph(FlowDefinition def, List<StepSpec> steps) {
        List<FlowGraph.FlowNode> nodes = new ArrayList<>();
        List<FlowGraph.FlowEdge> edges = new ArrayList<>();

        for (StepSpec spec : steps) {
            nodes.add(toActivityNode(spec));
        }

        for (int i = 0; i < nodes.size() - 1; i++) {
            FlowGraph.FlowNode from = nodes.get(i);
            FlowGraph.FlowNode to = nodes.get(i + 1);
            edges.add(new FlowGraph.FlowEdge(from.nodeId(), to.nodeId(), "", ""));
        }

        return createGraph(def, nodes, edges);
    }

    private FlowGraph buildBpmGraph(
            FlowDefinition def,
            List<StepSpec> steps,
            FlowStartEvent startEvent,
            FlowGateway[] gateways,
            FlowEndEvent[] endEvents
    ) {
        List<FlowGraph.FlowNode> nodes = new ArrayList<>();
        List<FlowGraph.FlowEdge> edges = new ArrayList<>();
        Map<String, FlowGraph.FlowNode> nodeById = new HashMap<>();

        if (startEvent != null) {
            FlowGraph.FlowNode startNode = new FlowGraph.FlowNode(
                    startEvent.nodeId(),
                    NodeType.START_EVENT,
                    0,
                    startEvent.name(),
                    startEvent.description(),
                    "",
                    "",
                    "",
                    "",
                    ""
            );
            putNode(nodes, nodeById, startNode, def.id());
            if (!startEvent.nextStep().isBlank()) {
                edges.add(new FlowGraph.FlowEdge(startEvent.nodeId(), startEvent.nextStep(), "", ""));
            }
        }

        for (StepSpec spec : steps) {
            FlowGraph.FlowNode activityNode = toActivityNode(spec);
            putNode(nodes, nodeById, activityNode, def.id());

            for (String nextStep : spec.step.nextSteps()) {
                if (nextStep != null && !nextStep.isBlank()) {
                    edges.add(new FlowGraph.FlowEdge(spec.nodeId, nextStep, "", ""));
                }
            }
        }

        for (FlowGateway gateway : gateways) {
            FlowGraph.FlowNode gatewayNode = new FlowGraph.FlowNode(
                    gateway.nodeId(),
                    gateway.type(),
                    0,
                    gateway.name(),
                    gateway.description(),
                    "",
                    "",
                    "",
                    "",
                    ""
            );
            putNode(nodes, nodeById, gatewayNode, def.id());

            for (FlowBranch branch : gateway.branches()) {
                edges.add(new FlowGraph.FlowEdge(gateway.nodeId(), branch.nextStep(), branch.label(), branch.edgeIntent()));
            }
        }

        for (FlowEndEvent endEvent : endEvents) {
            FlowGraph.FlowNode endNode = new FlowGraph.FlowNode(
                    endEvent.nodeId(),
                    NodeType.END_EVENT,
                    999,
                    endEvent.name(),
                    endEvent.description(),
                    "",
                    "",
                    "",
                    "",
                    ""
            );
            putNode(nodes, nodeById, endNode, def.id());
        }

        applyLegacyEdgesWhenNoExplicitLinks(nodes, edges);
        validateReadability(def, nodes, edges);
        return createGraph(def, nodes, edges);
    }

    private FlowGraph.FlowNode toActivityNode(StepSpec spec) {
        FlowStep step = spec.step;
        return new FlowGraph.FlowNode(
                spec.nodeId,
                NodeType.ACTIVITY,
                step.order(),
                step.name(),
                step.description(),
                step.purpose(),
                step.inputHint(),
                step.outputHint(),
                step.failureHint(),
                step.rollbackStep()
        );
    }

    private void putNode(
            List<FlowGraph.FlowNode> nodes,
            Map<String, FlowGraph.FlowNode> nodeById,
            FlowGraph.FlowNode node,
            String flowId
    ) {
        if (nodeById.containsKey(node.nodeId())) {
            log.warn("flow={} duplicated nodeId={} ignored", flowId, node.nodeId());
            return;
        }

        nodes.add(node);
        nodeById.put(node.nodeId(), node);
    }

    private void applyLegacyEdgesWhenNoExplicitLinks(List<FlowGraph.FlowNode> nodes, List<FlowGraph.FlowEdge> edges) {
        if (!edges.isEmpty()) {
            return;
        }

        List<FlowGraph.FlowNode> orderedActivities = nodes.stream()
                .filter(node -> node.type() == NodeType.ACTIVITY)
                .sorted(Comparator.comparingInt(FlowGraph.FlowNode::order))
                .toList();

        for (int i = 0; i < orderedActivities.size() - 1; i++) {
            FlowGraph.FlowNode from = orderedActivities.get(i);
            FlowGraph.FlowNode to = orderedActivities.get(i + 1);
            edges.add(new FlowGraph.FlowEdge(from.nodeId(), to.nodeId(), "", ""));
        }
    }

    private void validateReadability(FlowDefinition def, List<FlowGraph.FlowNode> nodes, List<FlowGraph.FlowEdge> edges) {
        for (FlowGraph.FlowNode node : nodes) {
            if (node.type() == NodeType.ACTIVITY
                    && (node.description().isBlank() || node.purpose().isBlank())) {
                log.warn("flow={} node={} missing description/purpose", def.id(), node.nodeId());
            }
        }

        for (FlowGraph.FlowNode node : nodes) {
            if (node.type() != NodeType.EXCLUSIVE_GATEWAY && node.type() != NodeType.PARALLEL_GATEWAY) {
                continue;
            }

            long unlabeled = edges.stream()
                    .filter(edge -> Objects.equals(edge.from(), node.nodeId()))
                    .filter(edge -> edge.label().isBlank())
                    .count();
            if (unlabeled > 0) {
                log.warn("flow={} gateway={} has unlabeled branches={}", def.id(), node.nodeId(), unlabeled);
            }
        }
    }

    private FlowGraph createGraph(FlowDefinition def, List<FlowGraph.FlowNode> nodes, List<FlowGraph.FlowEdge> edges) {
        return new FlowGraph(
                def.id(),
                def.name(),
                def.description(),
                def.owner(),
                def.version(),
                def.domainTag(),
                def.businessGoal(),
                List.copyOf(nodes),
                List.copyOf(edges)
        );
    }

    public List<FlowGraph> getAllFlows() {
        return graphs;
    }

    public FlowGraph findById(String id) {
        return graphs.stream()
                .filter(g -> g.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new FlowNotFoundException(id));
    }

    public FlowNarrative narrativeById(String id) {
        FlowGraph graph = findById(id);
        List<String> paths = buildNarrativePaths(graph);
        return new FlowNarrative(
                graph.id(),
                graph.name(),
                graph.businessGoal(),
                paths
        );
    }

    private List<String> buildNarrativePaths(FlowGraph graph) {
        Map<String, FlowGraph.FlowNode> nodeById = new HashMap<>();
        Map<String, List<FlowGraph.FlowEdge>> outgoingByNode = new HashMap<>();
        for (FlowGraph.FlowNode node : graph.nodes()) {
            nodeById.put(node.nodeId(), node);
        }
        for (FlowGraph.FlowEdge edge : graph.edges()) {
            outgoingByNode.computeIfAbsent(edge.from(), ignored -> new ArrayList<>()).add(edge);
        }

        String startId = graph.nodes().stream()
                .filter(node -> node.type() == NodeType.START_EVENT)
                .map(FlowGraph.FlowNode::nodeId)
                .findFirst()
                .orElseGet(() -> graph.nodes().stream()
                        .filter(node -> node.type() == NodeType.ACTIVITY)
                        .sorted(Comparator.comparingInt(FlowGraph.FlowNode::order))
                        .map(FlowGraph.FlowNode::nodeId)
                        .findFirst()
                        .orElse(""));

        if (startId.isBlank() || !nodeById.containsKey(startId)) {
            return List.of();
        }

        List<String> paths = new ArrayList<>();
        traverseNarrative(
                startId,
                nodeById,
                outgoingByNode,
                new ArrayList<>(),
                new HashSet<>(),
                paths
        );
        return List.copyOf(paths);
    }

    private void traverseNarrative(
            String nodeId,
            Map<String, FlowGraph.FlowNode> nodeById,
            Map<String, List<FlowGraph.FlowEdge>> outgoingByNode,
            List<String> segments,
            Set<String> guard,
            List<String> output
    ) {
        if (!guard.add(nodeId)) {
            return;
        }

        FlowGraph.FlowNode node = nodeById.get(nodeId);
        if (node == null) {
            guard.remove(nodeId);
            return;
        }

        segments.add(node.name());
        List<FlowGraph.FlowEdge> outgoing = outgoingByNode.getOrDefault(nodeId, List.of());
        if (outgoing.isEmpty()) {
            output.add(String.join(" -> ", segments));
            segments.remove(segments.size() - 1);
            guard.remove(nodeId);
            return;
        }

        for (FlowGraph.FlowEdge edge : outgoing) {
            List<String> branchSegments = new ArrayList<>(segments);
            if (!edge.label().isBlank()) {
                branchSegments.add("[" + edge.label() + "]");
            }
            traverseNarrative(edge.to(), nodeById, outgoingByNode, branchSegments, new HashSet<>(guard), output);
        }

        segments.remove(segments.size() - 1);
        guard.remove(nodeId);
    }

    private record StepSpec(String nodeId, FlowStep step) {
    }
}
