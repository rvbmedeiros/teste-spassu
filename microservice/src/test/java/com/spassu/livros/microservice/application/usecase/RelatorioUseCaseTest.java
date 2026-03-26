package com.spassu.livros.microservice.application.usecase;

import com.spassu.livros.microservice.application.dto.RelatorioLivroRow;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class RelatorioUseCaseTest {

        private static final Pattern REPORT_DATE_PATTERN = Pattern.compile("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}");

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private JasperReport relatorioLivrosReport;

    @Test
    @DisplayName("gerarPdf deve preencher e exportar relatorio")
    void gerarPdf_devePreencherEExportarRelatorio() throws Exception {
        RelatorioUseCase useCase = new RelatorioUseCase(jdbcTemplate, relatorioLivrosReport);
        JasperPrint print = new JasperPrint();
        byte[] pdf = "pdf".getBytes();
                AtomicReference<Map<String, Object>> capturedParameters = new AtomicReference<>();

        willReturn(List.of(RelatorioLivroRow.builder()
                .autorNome("Martin Fowler")
                .livroTitulo("Refactoring")
                .livroEditora("Addison-Wesley")
                .livroEdicao(2)
                .livroAnoPublicacao("2018")
                .livroValor(new BigDecimal("119.90"))
                .assuntos("Refatoração")
                .build())).given(jdbcTemplate)
                .query(contains("vw_relatorio_livros"), org.mockito.ArgumentMatchers.<RowMapper<RelatorioLivroRow>>any());

        try (MockedStatic<JasperFillManager> fillManager = mockStatic(JasperFillManager.class);
             MockedStatic<JasperExportManager> exportManager = mockStatic(JasperExportManager.class)) {
            fillManager.when(() -> JasperFillManager.fillReport(any(JasperReport.class), org.mockito.ArgumentMatchers.<String, Object>anyMap(), any(JRDataSource.class)))
                    .thenAnswer(invocation -> {
                        capturedParameters.set(invocation.getArgument(1));
                        return print;
                    });
            exportManager.when(() -> JasperExportManager.exportReportToPdf(print))
                    .thenReturn(pdf);

            byte[] result = useCase.gerarPdf();

            assertThat(result).isEqualTo(pdf);
                        assertThat(capturedParameters.get()).containsKeys("REPORT_GENERATED_AT", "REPORT_LOGO");
                        assertThat(capturedParameters.get().get("REPORT_GENERATED_AT")).isInstanceOf(String.class);
                        assertThat(REPORT_DATE_PATTERN.matcher(capturedParameters.get().get("REPORT_GENERATED_AT").toString()).matches())
                                        .isTrue();
        }
    }

    @Test
    @DisplayName("gerarPdf quando Jasper falha deve encapsular excecao")
    void gerarPdf_quandoJasperFalha_deveEncapsularExcecao() throws Exception {
        RelatorioUseCase useCase = new RelatorioUseCase(jdbcTemplate, relatorioLivrosReport);
        willReturn(List.<RelatorioLivroRow>of()).given(jdbcTemplate)
                .query(contains("vw_relatorio_livros"), org.mockito.ArgumentMatchers.<RowMapper<RelatorioLivroRow>>any());

        try (MockedStatic<JasperFillManager> fillManager = mockStatic(JasperFillManager.class)) {
            fillManager.when(() -> JasperFillManager.fillReport(any(JasperReport.class), org.mockito.ArgumentMatchers.<String, Object>anyMap(), any(JRDataSource.class)))
                    .thenThrow(new JRException("boom"));

            assertThatThrownBy(useCase::gerarPdf)
                    .isInstanceOf(RelatorioGenerationException.class)
                    .hasMessageContaining("Falha ao gerar relatório PDF");
        }
    }
}