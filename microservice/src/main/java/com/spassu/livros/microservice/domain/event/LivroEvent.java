package com.spassu.livros.microservice.domain.event;

import com.spassu.livros.microservice.domain.model.Livro;
import lombok.Getter;

import java.time.Instant;

@Getter
public class LivroEvent {

    public enum Type { CREATED, UPDATED, DELETED }

    private final Type    type;
    private final Integer livroCodL;
    private final String  livroTitulo;
    private final Instant occurredOn;

    private LivroEvent(Type type, Integer livroCodL, String livroTitulo) {
        this.type        = type;
        this.livroCodL   = livroCodL;
        this.livroTitulo = livroTitulo;
        this.occurredOn  = Instant.now();
    }

    public static LivroEvent created(Livro livro) {
        return new LivroEvent(Type.CREATED, livro.getCodL(), livro.getTitulo());
    }

    public static LivroEvent updated(Livro livro) {
        return new LivroEvent(Type.UPDATED, livro.getCodL(), livro.getTitulo());
    }

    public static LivroEvent deleted(Integer id) {
        return new LivroEvent(Type.DELETED, id, null);
    }
}
