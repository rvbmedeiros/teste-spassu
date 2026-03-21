package com.spassu.livros.orchestration.flowcockpit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

            @Test
            void buildRegistry_quandoGatewayTemApenasUmBranch_deveFalharNoStartup() {
            when(ctx.getBeansWithAnnotation(FlowDefinition.class))
                .thenReturn(Map.of("invalidGatewayFlow", new InvalidGatewayFlow()));

            assertThatThrownBy(() -> registry.buildRegistry())
                    .isInstanceOf(FlowValidationException.class)
                    .hasMessageContaining("must define exactly 2 branches");
            }

                @Test
                void buildRegistry_quandoGatewayExclusivoTemTresBranches_deveFalharNoStartup() {
                when(ctx.getBeansWithAnnotation(FlowDefinition.class))
                    .thenReturn(Map.of("invalidExclusiveGatewayFlow", new InvalidExclusiveGatewayFlow()));

                assertThatThrownBy(() -> registry.buildRegistry())
                    .isInstanceOf(FlowValidationException.class)
                    .hasMessageContaining("must define exactly 2 branches");
                }

                @Test
                void buildRegistry_quandoGatewayParaleloTemUmBranch_devePassar() {
                when(ctx.getBeansWithAnnotation(FlowDefinition.class))
                    .thenReturn(Map.of("validParallelSingleBranchFlow", new ValidParallelSingleBranchFlow()));

                registry.buildRegistry();

                assertThat(registry.findById("valid-parallel-single-branch").edges())
                    .extracting(edge -> edge.from() + "->" + edge.to())
                    .contains("gw-parallel->fim");
                }

                @Test
                void buildRegistry_quandoGatewayParaleloTemZeroBranch_deveFalharNoStartup() {
                when(ctx.getBeansWithAnnotation(FlowDefinition.class))
                    .thenReturn(Map.of("invalidParallelGatewayFlow", new InvalidParallelGatewayFlow()));

                assertThatThrownBy(() -> registry.buildRegistry())
                    .isInstanceOf(FlowValidationException.class)
                    .hasMessageContaining("must define at least 1 branch");
                }

            @Test
            void buildRegistry_quandoStepApontaParaNodeInexistente_deveFalharNoStartup() {
            when(ctx.getBeansWithAnnotation(FlowDefinition.class))
                .thenReturn(Map.of("invalidReferenceFlow", new InvalidReferenceFlow()));

            assertThatThrownBy(() -> registry.buildRegistry())
                .isInstanceOf(FlowValidationException.class)
                .hasMessageContaining("unknown node");
            }

        @Test
        @DisplayName("buildRegistry deve ligar passos em modo legado quando nao ha anotacoes BPM")
        void buildRegistry_deveLigarPassosEmModoLegadoQuandoNaoHaAnotacoesBpm() {
        when(ctx.getBeansWithAnnotation(FlowDefinition.class))
            .thenReturn(Map.of("legacyFlow", new LegacyFlow()));

        registry.buildRegistry();

        FlowGraph graph = registry.findById("legacy-flow");
        assertThat(graph.nodes()).extracting(FlowGraph.FlowNode::nodeId)
            .containsExactly("step-1", "step-2", "step-3");
        assertThat(graph.edges())
            .extracting(edge -> edge.from() + "->" + edge.to())
            .containsExactly("step-1->step-2", "step-2->step-3");
        }

        @Test
        @DisplayName("buildRegistry deve falhar com nodeId duplicado")
        void buildRegistry_deveFalharComNodeIdDuplicado() {
        when(ctx.getBeansWithAnnotation(FlowDefinition.class))
            .thenReturn(Map.of("duplicatedFlow", new DuplicatedNodeFlow()));

        assertThatThrownBy(() -> registry.buildRegistry())
            .isInstanceOf(FlowValidationException.class)
            .hasMessageContaining("duplicated nodeId");
        }

        @Test
        @DisplayName("narrativeById deve gerar caminhos por ramo do gateway")
        void narrativeById_deveGerarCaminhosPorRamoDoGateway() {
        when(ctx.getBeansWithAnnotation(FlowDefinition.class))
            .thenReturn(Map.of("branchingFlow", new BranchingFlow()));

        registry.buildRegistry();

        FlowNarrative narrative = registry.narrativeById("branching-flow");

        assertThat(narrative.paths())
            .containsExactlyInAnyOrder(
                "Início -> Validar -> Decisão -> [Aprovado] -> Publicar -> Fim",
                "Início -> Validar -> Decisão -> [Rejeitado] -> Corrigir -> Fim"
            );
        }

        @Test
        @DisplayName("narrativeById deve retornar vazio quando nao houver nos navegaveis")
        void narrativeById_deveRetornarVazioQuandoNaoHouverNosNavegaveis() {
        when(ctx.getBeansWithAnnotation(FlowDefinition.class))
            .thenReturn(Map.of("emptyFlow", new EmptyFlow()));

        registry.buildRegistry();

        FlowNarrative narrative = registry.narrativeById("empty-flow");

        assertThat(narrative.paths()).isEmpty();
        }

    @Test
    @DisplayName("buildRegistry deve resolver anotacoes no superclass do bean")
    void buildRegistry_deveResolverAnotacoesNoSuperclassDoBean() {
        when(ctx.getBeansWithAnnotation(FlowDefinition.class))
                .thenReturn(Map.of("proxiedFlow", new ProxiedFlow()));

        registry.buildRegistry();

        assertThat(registry.findById("proxied-flow").name()).isEqualTo("Fluxo Proxy");
    }

    @Test
    @DisplayName("buildRegistry BPM sem links explicitos deve ligar atividades em sequencia")
    void buildRegistryBpmSemLinksExplicitos_deveLigarAtividadesEmSequencia() {
        when(ctx.getBeansWithAnnotation(FlowDefinition.class))
                .thenReturn(Map.of("implicitBpmFlow", new ImplicitBpmFlow()));

        registry.buildRegistry();

        FlowGraph graph = registry.findById("implicit-bpm-flow");
        assertThat(graph.edges())
                .extracting(edge -> edge.from() + "->" + edge.to())
                .contains("step-1->step-2");
    }

    @Test
    @DisplayName("narrativeById deve usar primeira atividade quando nao houver start event")
    void narrativeById_deveUsarPrimeiraAtividadeQuandoNaoHouverStartEvent() {
        when(ctx.getBeansWithAnnotation(FlowDefinition.class))
                .thenReturn(Map.of("legacyFlow", new LegacyFlow()));

        registry.buildRegistry();

        FlowNarrative narrative = registry.narrativeById("legacy-flow");

        assertThat(narrative.paths()).containsExactly("Receber -> Processar -> Finalizar");
    }

    @Test
    @DisplayName("narrativeById deve evitar loop infinito em ciclos")
    void narrativeById_deveEvitarLoopInfinitoEmCiclos() {
        when(ctx.getBeansWithAnnotation(FlowDefinition.class))
                .thenReturn(Map.of("cyclicFlow", new CyclicFlow()));

        registry.buildRegistry();

        FlowNarrative narrative = registry.narrativeById("cyclic-flow");

        assertThat(narrative.paths()).isEmpty();
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

    @FlowDefinition(id = "invalid-gateway", name = "Fluxo Invalido Gateway")
    @FlowStartEvent(nextStep = "validar")
    @FlowGateway(
            nodeId = "gw-decisao",
            name = "Decision",
            type = NodeType.EXCLUSIVE_GATEWAY,
            branches = {
            @FlowBranch(label = "Apenas um", nextStep = "fim", edgeIntent = "single")
            }
    )
    @FlowEndEvent(nodeId = "fim")
    static class InvalidGatewayFlow {
        @FlowStep(order = 1, nodeId = "validar", name = "Validar", description = "", purpose = "", nextSteps = {"gw-decisao"})
        public void validar() {}
    }

        @FlowDefinition(id = "invalid-exclusive-gateway", name = "Fluxo Invalido Exclusivo")
        @FlowStartEvent(nextStep = "validar")
        @FlowGateway(
            nodeId = "gw-decisao",
            name = "Decision",
            type = NodeType.EXCLUSIVE_GATEWAY,
            branches = {
                @FlowBranch(label = "A", nextStep = "fim", edgeIntent = "a"),
                @FlowBranch(label = "B", nextStep = "fim", edgeIntent = "b"),
                @FlowBranch(label = "C", nextStep = "fim", edgeIntent = "c")
            }
        )
        @FlowEndEvent(nodeId = "fim")
        static class InvalidExclusiveGatewayFlow {
        @FlowStep(order = 1, nodeId = "validar", name = "Validar", description = "", purpose = "", nextSteps = {"gw-decisao"})
        public void validar() {}
        }

        @FlowDefinition(id = "valid-parallel-single-branch", name = "Fluxo Paralelo com Um Branch")
        @FlowStartEvent(nextStep = "validar")
        @FlowGateway(
            nodeId = "gw-parallel",
            name = "Paralelo",
            type = NodeType.PARALLEL_GATEWAY,
            branches = {
                @FlowBranch(label = "Unico", nextStep = "fim")
            }
        )
        @FlowEndEvent(nodeId = "fim")
        static class ValidParallelSingleBranchFlow {
        @FlowStep(order = 1, nodeId = "validar", name = "Validar", description = "", purpose = "", nextSteps = {"gw-parallel"})
        public void validar() {}
        }

        @FlowDefinition(id = "invalid-parallel-gateway", name = "Fluxo Invalido Paralelo")
        @FlowStartEvent(nextStep = "validar")
        @FlowGateway(
            nodeId = "gw-parallel",
            name = "Paralelo",
            type = NodeType.PARALLEL_GATEWAY,
            branches = {}
        )
        @FlowEndEvent(nodeId = "fim")
        static class InvalidParallelGatewayFlow {
        @FlowStep(order = 1, nodeId = "validar", name = "Validar", description = "", purpose = "", nextSteps = {"gw-parallel"})
        public void validar() {}
        }

    @FlowDefinition(id = "invalid-reference", name = "Fluxo Invalido Referencia")
    @FlowStartEvent(nextStep = "validar")
    @FlowEndEvent(nodeId = "fim")
    static class InvalidReferenceFlow {
        @FlowStep(order = 1, nodeId = "validar", name = "Validar", description = "", purpose = "", nextSteps = {"node-inexistente"})
        public void validar() {}
    }

    @FlowDefinition(id = "legacy-flow", name = "Fluxo Legado")
    static class LegacyFlow {
        @FlowStep(order = 1, name = "Receber", description = "Recebe", purpose = "Iniciar")
        void receber() {}

        @FlowStep(order = 2, name = "Processar", description = "Processa", purpose = "Executar")
        void processar() {}

        @FlowStep(order = 3, name = "Finalizar", description = "Finaliza", purpose = "Encerrar")
        void finalizar() {}
    }

    @FlowDefinition(id = "duplicated-flow", name = "Fluxo Duplicado")
    @FlowStartEvent(nextStep = "duplicado")
    @FlowEndEvent(nodeId = "fim")
    static class DuplicatedNodeFlow {
        @FlowStep(order = 1, nodeId = "duplicado", name = "Primeiro", description = "Desc", purpose = "Prop")
        void primeiro() {}

        @FlowStep(order = 2, nodeId = "duplicado", name = "Segundo", description = "Desc", purpose = "Prop", nextSteps = {"fim"})
        void segundo() {}
    }

    @FlowDefinition(id = "branching-flow", name = "Fluxo com Ramo", businessGoal = "Separar sucesso e falha")
    @FlowStartEvent(nextStep = "validar")
    @FlowGateway(
            nodeId = "gateway",
            name = "Decisão",
            type = NodeType.EXCLUSIVE_GATEWAY,
            branches = {
                @FlowBranch(label = "Aprovado", nextStep = "publicar", edgeIntent = "approved"),
                @FlowBranch(label = "Rejeitado", nextStep = "corrigir", edgeIntent = "rejected")
            }
    )
    @FlowEndEvent(nodeId = "fim", name = "Fim")
    static class BranchingFlow {
        @FlowStep(order = 1, nodeId = "validar", name = "Validar", description = "Valida", purpose = "Checar", nextSteps = {"gateway"})
        void validar() {}

        @FlowStep(order = 2, nodeId = "publicar", name = "Publicar", description = "Publica", purpose = "Concluir", nextSteps = {"fim"})
        void publicar() {}

        @FlowStep(order = 3, nodeId = "corrigir", name = "Corrigir", description = "Corrige", purpose = "Ajustar", nextSteps = {"fim"})
        void corrigir() {}
    }

    @FlowDefinition(id = "empty-flow", name = "Fluxo Vazio")
    static class EmptyFlow {
    }

    @FlowDefinition(id = "proxied-flow", name = "Fluxo Proxy")
    static class ProxiedFlowParent {
        @FlowStep(order = 1, name = "Executar", description = "Executa", purpose = "Rodar")
        void executar() {}
    }

    static class ProxiedFlow extends ProxiedFlowParent {
    }

    @FlowDefinition(id = "implicit-bpm-flow", name = "Fluxo BPM Implícito")
    @FlowStartEvent(nextStep = "")
    @FlowEndEvent(nodeId = "fim")
    static class ImplicitBpmFlow {
        @FlowStep(order = 1, name = "Primeiro", description = "Primeiro", purpose = "Iniciar")
        void primeiro() {}

        @FlowStep(order = 2, name = "Segundo", description = "Segundo", purpose = "Continuar")
        void segundo() {}
    }

    @FlowDefinition(id = "cyclic-flow", name = "Fluxo Cíclico")
    @FlowStartEvent(nextStep = "a")
    static class CyclicFlow {
        @FlowStep(order = 1, nodeId = "a", name = "A", description = "A", purpose = "A", nextSteps = {"b"})
        void a() {}

        @FlowStep(order = 2, nodeId = "b", name = "B", description = "B", purpose = "B", nextSteps = {"a"})
        void b() {}
    }
}
