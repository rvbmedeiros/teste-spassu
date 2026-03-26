-- =============================================================================
-- V2__view.sql  — Relatório view grouped by Autor
--                 (used by JasperReports datasource query)
-- =============================================================================

CREATE OR REPLACE VIEW vw_relatorio_livros AS
SELECT
    a.cod_au                                        AS autor_cod,
    a.nome                                          AS autor_nome,
    l.cod_l                                         AS livro_cod,
    l.titulo                                        AS livro_titulo,
    l.editora                                       AS livro_editora,
    l.edicao                                        AS livro_edicao,
    l.ano_publicacao                                AS livro_ano_publicacao,
    l.valor                                         AS livro_valor,
    STRING_AGG(DISTINCT s.descricao, ', '
               ORDER BY s.descricao)                AS assuntos
FROM   autor            a
JOIN   livro_autor      la  ON la.autor_cod_au  = a.cod_au
JOIN   livro            l   ON l.cod_l          = la.livro_cod_l
LEFT JOIN livro_assunto las ON las.livro_cod_l  = l.cod_l
LEFT JOIN assunto       s   ON s.cod_as         = las.assunto_cod_as
GROUP BY a.cod_au, a.nome, l.cod_l, l.titulo, l.editora, l.edicao,
         l.ano_publicacao, l.valor
ORDER BY a.nome, l.titulo;
