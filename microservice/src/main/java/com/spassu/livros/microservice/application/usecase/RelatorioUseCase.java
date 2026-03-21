package com.spassu.livros.microservice.application.usecase;

import com.spassu.livros.microservice.application.dto.RelatorioLivroRow;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RelatorioUseCase {

    private final JdbcTemplate   jdbcTemplate;
    private final JasperReport   relatorioLivrosReport;

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

        try {
            JasperPrint print = JasperFillManager.fillReport(
                    relatorioLivrosReport,
                    new HashMap<>(),
                    new JRBeanCollectionDataSource(rows));
            return JasperExportManager.exportReportToPdf(print);
        } catch (JRException ex) {
            throw new RelatorioGenerationException("Falha ao gerar relatório PDF", ex);
        }
    }
}
