package com.spassu.livros.microservice.application.usecase;

import com.spassu.livros.microservice.application.dto.RelatorioLivroRow;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RelatorioUseCase {

    private static final DateTimeFormatter REPORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final JdbcTemplate   jdbcTemplate;
    private final JasperReport   relatorioLivrosReport;

    public RelatorioUseCase(JdbcTemplate jdbcTemplate,
                            @Lazy JasperReport relatorioLivrosReport) {
        this.jdbcTemplate = jdbcTemplate;
        this.relatorioLivrosReport = relatorioLivrosReport;
    }

    private static final String QUERY = """
            SELECT autor_nome, livro_titulo, livro_editora, livro_edicao,
                   livro_ano_publicacao, livro_valor, assuntos
            FROM vw_relatorio_livros
            ORDER BY autor_nome, livro_titulo
            """;

    public byte[] gerarPdf() {
        List<RelatorioLivroRow> rows = jdbcTemplate.query(QUERY, (rs, rowNum) ->
                RelatorioLivroRow.builder()
                        .autorNome(rs.getString("autor_nome"))
                        .livroTitulo(rs.getString("livro_titulo"))
                        .livroEditora(rs.getString("livro_editora"))
                        .livroEdicao(rs.getInt("livro_edicao"))
                        .livroAnoPublicacao(rs.getString("livro_ano_publicacao"))
                        .livroValor(rs.getBigDecimal("livro_valor"))
                        .assuntos(rs.getString("assuntos"))
                        .build());

        try (InputStream logo = carregarLogo()) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("REPORT_GENERATED_AT", LocalDateTime.now().format(REPORT_DATE_FORMATTER));
            parameters.put("REPORT_LOGO", logo);

            JasperPrint print = JasperFillManager.fillReport(
                    relatorioLivrosReport,
                    parameters,
                    new JRBeanCollectionDataSource(rows));
            return JasperExportManager.exportReportToPdf(print);
        } catch (JRException | IOException ex) {
            throw new RelatorioGenerationException("Falha ao gerar relatório PDF", ex);
        }
    }

    private InputStream carregarLogo() throws IOException {
        ClassPathResource logoResource = new ClassPathResource("reports/images/logo.png");
        if (!logoResource.exists()) {
            return null;
        }
        return logoResource.getInputStream();
    }
}
