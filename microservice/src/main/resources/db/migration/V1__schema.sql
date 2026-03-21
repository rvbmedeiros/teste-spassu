-- =============================================================================
-- V1__schema.sql  — DDL following the exact data model from modelagem-tabelas.png
--                    + Valor field added per business requirement
-- =============================================================================

CREATE TABLE autor (
    cod_au    SERIAL       PRIMARY KEY,
    nome      VARCHAR(40)  NOT NULL
);

CREATE TABLE assunto (
    cod_as    SERIAL      PRIMARY KEY,
    descricao VARCHAR(20) NOT NULL
);

CREATE TABLE livro (
    cod_l          SERIAL          PRIMARY KEY,
    titulo         VARCHAR(40)     NOT NULL,
    editora        VARCHAR(40),
    edicao         INTEGER,
    ano_publicacao VARCHAR(4),
    valor          NUMERIC(10, 2)  NOT NULL DEFAULT 0.00
);

CREATE TABLE livro_autor (
    livro_cod_l   INTEGER NOT NULL
        REFERENCES livro(cod_l)  ON DELETE CASCADE ON UPDATE CASCADE,
    autor_cod_au  INTEGER NOT NULL
        REFERENCES autor(cod_au) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT pk_livro_autor PRIMARY KEY (livro_cod_l, autor_cod_au)
);

CREATE INDEX livro_autor_fk_idx1 ON livro_autor(livro_cod_l);
CREATE INDEX livro_autor_fk_idx2 ON livro_autor(autor_cod_au);

CREATE TABLE livro_assunto (
    livro_cod_l    INTEGER NOT NULL
        REFERENCES livro(cod_l)    ON DELETE CASCADE ON UPDATE CASCADE,
    assunto_cod_as INTEGER NOT NULL
        REFERENCES assunto(cod_as) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT pk_livro_assunto PRIMARY KEY (livro_cod_l, assunto_cod_as)
);

CREATE INDEX livro_assunto_fk_idx1 ON livro_assunto(livro_cod_l);
CREATE INDEX livro_assunto_fk_idx2 ON livro_assunto(assunto_cod_as);
