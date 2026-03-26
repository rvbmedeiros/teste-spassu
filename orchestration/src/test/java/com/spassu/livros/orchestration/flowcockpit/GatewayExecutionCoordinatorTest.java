package com.spassu.livros.orchestration.flowcockpit;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayExecutionCoordinatorTest {

    @Test
    void routeExclusive_deveExecutarSomenteRamoSelecionado() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setThreadNamePrefix("flow-parallel-test-");
        executor.initialize();

        GatewayExecutionCoordinator coordinator = new GatewayExecutionCoordinator(executor);
        AtomicBoolean approvedExecuted = new AtomicBoolean(false);
        AtomicBoolean rejectedExecuted = new AtomicBoolean(false);

        Mono<String> result = coordinator.routeExclusive(
                "flow-test",
                "gw-validacao",
                "approved",
                Map.of(
                        "approved", () -> Mono.fromCallable(() -> {
                            approvedExecuted.set(true);
                            return "ok";
                        }),
                        "rejected", () -> Mono.fromCallable(() -> {
                            rejectedExecuted.set(true);
                            return "no";
                        })
                )
        );

        StepVerifier.create(result)
                .expectNext("ok")
                .verifyComplete();

        assertThat(approvedExecuted).isTrue();
        assertThat(rejectedExecuted).isFalse();
        executor.shutdown();
    }

    @Test
    void routeExclusive_quandoIntentNaoMapeado_deveFalhar() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.initialize();

        GatewayExecutionCoordinator coordinator = new GatewayExecutionCoordinator(executor);

        Mono<String> result = coordinator.routeExclusive(
                "flow-test",
                "gw-validacao",
                "unknown",
                Map.of("approved", () -> Mono.just("ok"))
        );

        StepVerifier.create(result)
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(FlowExecutionException.class);
                    assertThat(error.getMessage()).contains("has no route for edgeIntent");
                })
                .verify();

        executor.shutdown();
    }

    @Test
    void runParallel_deveExecutarTodosOsRamosNoExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setThreadNamePrefix("flow-parallel-test-");
        executor.initialize();

        GatewayExecutionCoordinator coordinator = new GatewayExecutionCoordinator(executor);
        Set<String> threadNames = ConcurrentHashMap.newKeySet();

        Mono<Void> run = coordinator.runParallel(
                "flow-test",
                "gw-prepare",
                Map.of(
                        "a", () -> Mono.fromRunnable(() -> threadNames.add(Thread.currentThread().getName())),
                        "b", () -> Mono.fromRunnable(() -> threadNames.add(Thread.currentThread().getName()))
                )
        );

        StepVerifier.create(run)
                .verifyComplete();

        assertThat(threadNames).hasSizeGreaterThanOrEqualTo(2);
        assertThat(threadNames).allMatch(name -> name.startsWith("flow-parallel-test-"));
        executor.shutdown();
    }
}
