-- =============================================================================
-- V3__seed.sql  — DML seed data (ready-to-use on first boot)
-- =============================================================================

-- Autores
INSERT INTO autor (nome) VALUES
    ('Martin Fowler'),
    ('Robert C. Martin'),
    ('Erich Gamma'),
    ('Joshua Bloch'),
    ('Vaughn Vernon');

-- Assuntos
INSERT INTO assunto (descricao) VALUES
    ('Arquitetura'),
    ('Design Patterns'),
    ('Clean Code'),
    ('Domain-Driven'),
    ('Java'),
    ('Refactoring');

-- Livros
INSERT INTO livro (titulo, editora, edicao, ano_publicacao, valor) VALUES
    ('Refactoring',                      'Addison-Wesley', 2, '2018', 249.90),
    ('Clean Code',                       'Prentice Hall',  1, '2008', 199.90),
    ('Design Patterns',                  'Addison-Wesley', 1, '1994', 299.90),
    ('Effective Java',                   'Addison-Wesley', 3, '2018', 219.90),
    ('Implementing Domain-Driven Design','Addison-Wesley', 1, '2013', 289.90),
    ('Patterns of Enterprise Application Architecture', 'Addison-Wesley', 1, '2002', 319.90);

-- Livro ↔ Autor
-- Refactoring → Martin Fowler
INSERT INTO livro_autor (livro_cod_l, autor_cod_au) VALUES (1, 1);
-- Clean Code → Robert C. Martin
INSERT INTO livro_autor (livro_cod_l, autor_cod_au) VALUES (2, 2);
-- Design Patterns → Erich Gamma  (multiple authors — only one stored to match model)
INSERT INTO livro_autor (livro_cod_l, autor_cod_au) VALUES (3, 3);
-- Effective Java → Joshua Bloch
INSERT INTO livro_autor (livro_cod_l, autor_cod_au) VALUES (4, 4);
-- IDDD → Vaughn Vernon
INSERT INTO livro_autor (livro_cod_l, autor_cod_au) VALUES (5, 5);
-- PoEAA → Martin Fowler
INSERT INTO livro_autor (livro_cod_l, autor_cod_au) VALUES (6, 1);
-- Demo: Clean Code also by Erich Gamma (demonstrating multi-author)
INSERT INTO livro_autor (livro_cod_l, autor_cod_au) VALUES (2, 3);

-- Livro ↔ Assunto
INSERT INTO livro_assunto (livro_cod_l, assunto_cod_as) VALUES
    (1, 6), (1, 1),  -- Refactoring → Refactoring, Arquitetura
    (2, 3),           -- Clean Code → Clean Code
    (3, 2),           -- Design Patterns → Design Patterns
    (4, 5),           -- Effective Java → Java
    (5, 4), (5, 1),  -- IDDD → Domain-Driven, Arquitetura
    (6, 1), (6, 2);  -- PoEAA → Arquitetura, Design Patterns
