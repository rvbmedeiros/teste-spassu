package com.spassu.livros.orchestration.flowcockpit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
@Component
public class GatewayExecutionCoordinator {

    private final Scheduler flowParallelScheduler;

    public GatewayExecutionCoordinator(@Qualifier("flowParallelExecutor") TaskExecutor flowParallelExecutor) {
        this.flowParallelScheduler = Schedulers.fromExecutor(flowParallelExecutor);
    }

    public <T> Mono<T> routeExclusive(
            String flowId,
            String gatewayNodeId,
            String selectedEdgeIntent,
            Map<String, Supplier<Mono<T>>> routesByEdgeIntent
    ) {
        Supplier<Mono<T>> route = routesByEdgeIntent.get(selectedEdgeIntent);
        if (route == null) {
            return Mono.error(new FlowExecutionException(
                    "flow=" + flowId + " gateway='" + gatewayNodeId + "' has no route for edgeIntent='" + selectedEdgeIntent + "'"
            ));
        }

        log.debug("flow={} gateway={} selectedEdgeIntent={}", flowId, gatewayNodeId, selectedEdgeIntent);
        return Mono.defer(route);
    }

    public Mono<Void> runParallel(
            String flowId,
            String gatewayNodeId,
            Map<String, Supplier<Mono<?>>> branchesByEdgeIntent
    ) {
        if (branchesByEdgeIntent.isEmpty()) {
            return Mono.error(new FlowExecutionException(
                    "flow=" + flowId + " gateway='" + gatewayNodeId + "' must define at least 1 runtime branch"
            ));
        }

        List<Mono<?>> executions = new ArrayList<>(branchesByEdgeIntent.size());
        for (Map.Entry<String, Supplier<Mono<?>>> entry : branchesByEdgeIntent.entrySet()) {
            String edgeIntent = entry.getKey();
            Supplier<Mono<?>> branch = entry.getValue();

            executions.add(Mono.defer(branch)
                    .doOnSubscribe(ignored -> log.debug("flow={} gateway={} branch={} started", flowId, gatewayNodeId, edgeIntent))
                    .doOnSuccess(ignored -> log.debug("flow={} gateway={} branch={} completed", flowId, gatewayNodeId, edgeIntent))
                    .doOnError(ex -> log.warn("flow={} gateway={} branch={} failed", flowId, gatewayNodeId, edgeIntent, ex))
                    .subscribeOn(flowParallelScheduler));
        }

        return Mono.whenDelayError(executions);
    }
}