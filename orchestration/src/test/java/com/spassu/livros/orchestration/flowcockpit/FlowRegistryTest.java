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
        assertThat(graph.nodes()).hasSize(3);
        assertThat(graph.nodes()).anySatisfy(node -> {
            assertThat(node.nodeId()).isEqualTo("start");
            assertThat(node.type()).isEqualTo(NodeType.START_EVENT);
        });
        assertThat(graph.nodes()).anySatisfy(node -> {
            assertThat(node.nodeId()).isEqualTo("validar");
            assertThat(node.type()).isEqualTo(NodeType.ACTIVITY);
        });
        assertThat(graph.nodes()).anySatisfy(node -> {
            assertThat(node.nodeId()).isEqualTo("fim");
            assertThat(node.type()).isEqualTo(NodeType.END_EVENT);
        });
        assertThat(graph.edges()).hasSize(2);
    }

    @Test
    void findById_quandoNaoEncontrado_deveLancarException() {
        when(ctx.getBeansWithAnnotation(FlowDefinition.class)).thenReturn(Map.of());
        registry.buildRegistry();

        assertThatThrownBy(() -> registry.findById("nao-existe"))
                .isInstanceOf(FlowNotFoundException.class);
    }

    @FlowDefinition(id = "test-flow", name = "Fluxo de Teste")
    @FlowStartEvent(nextStep = "validar")
    @FlowEndEvent(nodeId = "fim")
    static class SampleFlow {
        @FlowStep(
                order = 1,
                nodeId = "validar",
                name = "Passo 1",
                description = "Valida",
                purpose = "Garantir consistencia",
                nextSteps = {"fim"}
        )
        public void paso1() {}
    }
}
