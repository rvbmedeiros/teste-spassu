package com.spassu.livros.microservice.infrastructure.persistence.mapper;

import com.spassu.livros.microservice.domain.model.Assunto;
import com.spassu.livros.microservice.domain.model.Autor;
import com.spassu.livros.microservice.domain.model.Livro;
import com.spassu.livros.microservice.infrastructure.persistence.entity.AssuntoEntity;
import com.spassu.livros.microservice.infrastructure.persistence.entity.AutorEntity;
import com.spassu.livros.microservice.infrastructure.persistence.entity.LivroAssuntoEntity;
import com.spassu.livros.microservice.infrastructure.persistence.entity.LivroAssuntoId;
import com.spassu.livros.microservice.infrastructure.persistence.entity.LivroAutorEntity;
import com.spassu.livros.microservice.infrastructure.persistence.entity.LivroAutorId;
import com.spassu.livros.microservice.infrastructure.persistence.entity.LivroEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LivroEntityMapperTest {

    @Mock
    private AutorEntityMapper autorMapper;

    @Mock
    private AssuntoEntityMapper assuntoMapper;

    @Test
    @DisplayName("toDomain deve retornar null quando entidade for null")
    void toDomain_deveRetornarNullQuandoEntidadeForNull() {
        LivroEntityMapper mapper = mapper();

        assertThat(mapper.toDomain(null)).isNull();
    }

    @Test
    @DisplayName("toDomain deve mapear autores e assuntos das tabelas de associacao")
    void toDomain_deveMapearAutoresEAssuntosDasTabelasDeAssociacao() {
        LivroEntity entity = LivroEntity.builder()
                .codL(1)
                .titulo("Clean Architecture")
                .editora("Pearson")
                .edicao(1)
                .anoPublicacao("2017")
                .valor(new BigDecimal("149.90"))
                .autores(new HashSet<>())
                .assuntos(new HashSet<>())
                .build();
        AutorEntity autorEntity = AutorEntity.builder().codAu(5).nome("Uncle Bob").build();
        AssuntoEntity assuntoEntity = AssuntoEntity.builder().codAs(9).descricao("Arquitetura").build();
        entity.getAutores().add(new LivroAutorEntity(new LivroAutorId(1, 5), entity, autorEntity));
        entity.getAssuntos().add(new LivroAssuntoEntity(new LivroAssuntoId(1, 9), entity, assuntoEntity));
        Autor autor = Autor.builder().codAu(5).nome("Uncle Bob").build();
        Assunto assunto = Assunto.builder().codAs(9).descricao("Arquitetura").build();

        given(autorMapper.toDomain(autorEntity)).willReturn(autor);
        given(assuntoMapper.toDomain(assuntoEntity)).willReturn(assunto);

        Livro result = mapper().toDomain(entity);

        assertThat(result.getCodL()).isEqualTo(1);
        assertThat(result.getTitulo()).isEqualTo("Clean Architecture");
        assertThat(result.getAutores()).containsExactly(autor);
        assertThat(result.getAssuntos()).containsExactly(assunto);
    }

    private LivroEntityMapper mapper() {
        LivroEntityMapper mapper = new LivroEntityMapper() {
            @Override
            public LivroEntity toEntity(Livro domain) {
                return null;
            }

            @Override
            public void updateScalars(LivroEntity entity, Livro domain) {
            }
        };
        mapper.autorMapper = autorMapper;
        mapper.assuntoMapper = assuntoMapper;
        return mapper;
    }
}