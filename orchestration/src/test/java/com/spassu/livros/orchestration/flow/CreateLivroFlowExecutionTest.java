package com.spassu.livros.orchestration.flow;

import com.spassu.livros.orchestration.client.MicroserviceClient;
import com.spassu.livros.orchestration.dto.LivroRequest;
import com.spassu.livros.orchestration.dto.LivroResponse;
import com.spassu.livros.orchestration.flowcockpit.GatewayExecutionCoordinator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class CreateLivroFlowExecutionTest {

    @Mock
    private MicroserviceClient client;

    @Test
    void execute_quandoPayloadValido_devePersistir() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setThreadNamePrefix("flow-parallel-test-");
        executor.initialize();

        GatewayExecutionCoordinator coordinator = new GatewayExecutionCoordinator(executor);
        CreateLivroFlow flow = new CreateLivroFlow(client, coordinator);
        LivroRequest request = requestValido();

        given(client.criarLivro(request)).willReturn(Mono.just(new LivroResponse(
                1,
                request.titulo(),
                request.editora(),
                request.edicao(),
                request.anoPublicacao(),
                request.valor(),
                List.of(),
                List.of()
        )));

        StepVerifier.create(flow.execute(request))
                .expectNextCount(1)
                .verifyComplete();

        then(client).should().criarLivro(request);
        executor.shutdown();
    }

    @Test
    void execute_quandoValorZero_devePersistir() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setThreadNamePrefix("flow-parallel-test-");
        executor.initialize();

        GatewayExecutionCoordinator coordinator = new GatewayExecutionCoordinator(executor);
        CreateLivroFlow flow = new CreateLivroFlow(client, coordinator);
        LivroRequest request = new LivroRequest(
                "Domain Driven Design",
                "Alta Books",
                1,
                "2024",
                BigDecimal.ZERO,
                Set.of(1),
                Set.of(10)
        );

        given(client.criarLivro(request)).willReturn(Mono.just(new LivroResponse(
                1,
                request.titulo(),
                request.editora(),
                request.edicao(),
                request.anoPublicacao(),
                request.valor(),
                List.of(),
                List.of()
        )));

        StepVerifier.create(flow.execute(request))
                .expectNextCount(1)
                .verifyComplete();

        then(client).should().criarLivro(request);
        executor.shutdown();
    }

    @Test
    void execute_quandoPayloadInvalido_deveFalharSemPersistir() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.initialize();

        GatewayExecutionCoordinator coordinator = new GatewayExecutionCoordinator(executor);
        CreateLivroFlow flow = new CreateLivroFlow(client, coordinator);

        LivroRequest requestInvalido = new LivroRequest(
                "",
                "",
                0,
                "",
                BigDecimal.ZERO,
                Set.of(),
                Set.of()
        );

        StepVerifier.create(flow.execute(requestInvalido))
                .expectError(IllegalArgumentException.class)
                .verify();

        then(client).should(never()).criarLivro(requestInvalido);
        executor.shutdown();
    }

    private LivroRequest requestValido() {
        return new LivroRequest(
                "Domain Driven Design",
                "Alta Books",
                1,
                "2024",
                new BigDecimal("99.90"),
                Set.of(1),
                Set.of(10)
        );
    }
}
