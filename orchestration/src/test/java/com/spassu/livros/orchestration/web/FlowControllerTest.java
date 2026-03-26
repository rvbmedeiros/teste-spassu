package com.spassu.livros.orchestration.web;

import com.spassu.livros.orchestration.flowcockpit.FlowGraph;
import com.spassu.livros.orchestration.flowcockpit.FlowNarrative;
import com.spassu.livros.orchestration.flowcockpit.FlowRegistry;
import com.spassu.livros.orchestration.flowcockpit.NodeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class FlowControllerTest {

    @Mock
    private FlowRegistry registry;

    @InjectMocks
    private FlowController controller;

    @Test
    @DisplayName("narrativa deve delegar ao registry")
    void narrativa_deveDelegarAoRegistry() {
        FlowNarrative narrative = new FlowNarrative(
                "create-livro",
                "Criar Livro",
                "Cadastrar livro",
                List.of("Início -> Validar -> Fim")
        );
        given(registry.narrativeById("create-livro")).willReturn(narrative);

        FlowNarrative result = controller.narrativa("create-livro");

        assertThat(result).isEqualTo(narrative);
        then(registry).should().narrativeById("create-livro");
    }

    @Test
    @DisplayName("listar deve retornar fluxos")
    void listar_deveRetornarFluxos() {
        FlowGraph graph = new FlowGraph(
                "id",
                "nome",
                "desc",
                "owner",
                "1.0",
                "domain",
                "goal",
                List.of(new FlowGraph.FlowNode("n1", NodeType.ACTIVITY, 1, "Passo", "", "", "", "", "", "")),
                List.of()
        );
        given(registry.getAllFlows()).willReturn(List.of(graph));

        List<FlowGraph> result = controller.listar();

        assertThat(result).hasSize(1);
        then(registry).should().getAllFlows();
    }
}
