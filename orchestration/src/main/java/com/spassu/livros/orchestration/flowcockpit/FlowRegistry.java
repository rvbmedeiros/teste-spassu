package com.spassu.livros.orchestration.flowcockpit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Scans the Spring context at startup for all beans annotated with
 * {@link FlowDefinition} and builds an immutable list of {@link FlowGraph}
 * objects representing the static DAG of each orchestration flow.
 */
@Slf4j
@Component
public class FlowRegistry implements ApplicationContextAware {

    private ApplicationContext ctx;
    private List<FlowGraph> graphs = List.of();

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
        this.ctx = applicationContext;
    }

    @PostConstruct
    void buildRegistry() {
        Map<String, Object> beans = ctx.getBeansWithAnnotation(FlowDefinition.class);
        List<FlowGraph> result = new ArrayList<>(beans.size());

        for (Object bean : beans.values()) {
            FlowDefinition def = bean.getClass().getAnnotation(FlowDefinition.class);
            // Also handle Spring CGLIB proxies — check superclass annotation
            if (def == null) {
                def = bean.getClass().getSuperclass().getAnnotation(FlowDefinition.class);
            }
            if (def == null) continue;

            List<FlowGraph.FlowNode> nodes = new ArrayList<>();
            Class<?> target = bean.getClass().getSuperclass() != null
                              && bean.getClass().getSuperclass().isAnnotationPresent(FlowDefinition.class)
                    ? bean.getClass().getSuperclass()
                    : bean.getClass();

            for (Method m : target.getDeclaredMethods()) {
                FlowStep step = m.getAnnotation(FlowStep.class);
                if (step != null) {
                    nodes.add(new FlowGraph.FlowNode(
                            step.order(), step.name(), step.description(), step.rollbackStep()));
                }
            }
            nodes.sort(Comparator.comparingInt(FlowGraph.FlowNode::order));
            result.add(new FlowGraph(def.id(), def.name(), def.description(), List.copyOf(nodes)));
        }
        this.graphs = List.copyOf(result);
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
}
