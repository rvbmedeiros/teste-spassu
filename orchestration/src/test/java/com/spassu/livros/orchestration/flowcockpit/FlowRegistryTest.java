package com.spassu.livros.orchestration.flowcockpit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlowRegistryTest {

    @Mock
    private ApplicationContext ctx;

    private FlowRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new FlowRegistry();
        registry.setApplicationContext(ctx);
    }

    @Test
    void buildRegistry_deveRegistrarFluxosAnotados() {
        SampleFlow sampleFlow = new SampleFlow();
        when(ctx.getBeansWithAnnotation(FlowDefinition.class))
                .thenReturn(Map.of("sampleFlow", sampleFlow));

        registry.buildRegistry();

        assertThat(registry.getAllFlows()).hasSize(1);
        FlowGraph graph = registry.getAllFlows().get(0);
        assertThat(graph.id()).isEqualTo("test-flow");
        assertThat(graph.name()).isEqualTo("Fluxo de Teste");
        assertThat(graph.steps()).hasSize(1);
        assertThat(graph.steps().get(0).order()).isEqualTo(1);
    }

    @Test
    void findById_quandoNaoEncontrado_deveLancarException() {
        when(ctx.getBeansWithAnnotation(FlowDefinition.class)).thenReturn(Map.of());
        registry.buildRegistry();

        assertThatThrownBy(() -> registry.findById("nao-existe"))
                .isInstanceOf(FlowNotFoundException.class);
    }

    @FlowDefinition(id = "test-flow", name = "Fluxo de Teste")
    static class SampleFlow {
        @FlowStep(order = 1, name = "Passo 1")
        public void paso1() {}
    }
}
