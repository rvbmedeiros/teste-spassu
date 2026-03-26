package com.spassu.livros.orchestration.flow;

import com.spassu.livros.orchestration.client.MicroserviceClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class GetRelatorioFlowTest {

    @Mock
    private MicroserviceClient client;

    @InjectMocks
    private GetRelatorioFlow flow;

    @Test
    @DisplayName("execute deve delegar a geracao de relatorio para o microservice")
    void execute_deveDelegarAGeracaoDeRelatorioParaOMicroservice() {
        var pdf = new byte[] {1, 2, 3, 4};
        given(client.gerarRelatorio()).willReturn(Mono.just(pdf));

        var result = flow.execute().block();

        assertThat(result).isEqualTo(pdf);
        then(client).should().gerarRelatorio();
    }
}
