package com.spassu.livros.microservice.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitMqConfig {

    // ── exchange names ────────────────────────────────────────────────────────
    public static final String EXCHANGE     = "livro.events";
    public static final String EXCHANGE_DLX = "livro.events.dlx";

    // ── queue names ───────────────────────────────────────────────────────────
    public static final String QUEUE_CREATED = "livro.created";
    public static final String QUEUE_UPDATED = "livro.updated";
    public static final String QUEUE_DELETED = "livro.deleted";
    public static final String QUEUE_DLQ     = "livro.events.dlq";

    // ── routing keys ──────────────────────────────────────────────────────────
    public static final String RK_CREATED = "livro.created";
    public static final String RK_UPDATED = "livro.updated";
    public static final String RK_DELETED = "livro.deleted";

    // ── RPC exchange names ───────────────────────────────────────────────────
    public static final String RPC_EXCHANGE = "livros.rpc";

    // ── RPC queue names ──────────────────────────────────────────────────────
    public static final String RPC_LIVROS_CRIAR = "rpc.livros.criar";
    public static final String RPC_LIVROS_LISTAR = "rpc.livros.listar";
    public static final String RPC_LIVROS_BUSCAR = "rpc.livros.buscar";
    public static final String RPC_LIVROS_ATUALIZAR = "rpc.livros.atualizar";
    public static final String RPC_LIVROS_EXCLUIR = "rpc.livros.excluir";

    public static final String RPC_AUTORES_CRIAR = "rpc.autores.criar";
    public static final String RPC_AUTORES_LISTAR = "rpc.autores.listar";
    public static final String RPC_AUTORES_BUSCAR = "rpc.autores.buscar";
    public static final String RPC_AUTORES_ATUALIZAR = "rpc.autores.atualizar";
    public static final String RPC_AUTORES_EXCLUIR = "rpc.autores.excluir";

    public static final String RPC_ASSUNTOS_CRIAR = "rpc.assuntos.criar";
    public static final String RPC_ASSUNTOS_LISTAR = "rpc.assuntos.listar";
    public static final String RPC_ASSUNTOS_BUSCAR = "rpc.assuntos.buscar";
    public static final String RPC_ASSUNTOS_ATUALIZAR = "rpc.assuntos.atualizar";
    public static final String RPC_ASSUNTOS_EXCLUIR = "rpc.assuntos.excluir";

    public static final String RPC_RELATORIO_GERAR = "rpc.relatorio.gerar";

    // ─── RabbitAdmin: declara exchanges/queues/bindings no boot ──────────────
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    // ─── Exchanges ────────────────────────────────────────────────────────────
    @Bean
    public TopicExchange livroEventsExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    public DirectExchange livroEventsDlx() {
        return ExchangeBuilder.directExchange(EXCHANGE_DLX).durable(true).build();
    }

    // ─── Queues ───────────────────────────────────────────────────────────────
    @Bean
    public Queue livroCreatedQueue() {
        return QueueBuilder.durable(QUEUE_CREATED)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .build();
    }

    @Bean
    public Queue livroUpdatedQueue() {
        return QueueBuilder.durable(QUEUE_UPDATED)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .build();
    }

    @Bean
    public Queue livroDeletedQueue() {
        return QueueBuilder.durable(QUEUE_DELETED)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .build();
    }

    @Bean
    public Queue livroEventsDlq() {
        return QueueBuilder.durable(QUEUE_DLQ).build();
    }

    // ─── Bindings ─────────────────────────────────────────────────────────────
    @Bean
    public Binding bindingCreated() {
        return BindingBuilder.bind(livroCreatedQueue()).to(livroEventsExchange()).with(RK_CREATED);
    }

    @Bean
    public Binding bindingUpdated() {
        return BindingBuilder.bind(livroUpdatedQueue()).to(livroEventsExchange()).with(RK_UPDATED);
    }

    @Bean
    public Binding bindingDeleted() {
        return BindingBuilder.bind(livroDeletedQueue()).to(livroEventsExchange()).with(RK_DELETED);
    }

    // ─── RPC exchange ────────────────────────────────────────────────────────
    @Bean
    public DirectExchange livrosRpcExchange() {
        return ExchangeBuilder.directExchange(RPC_EXCHANGE).durable(true).build();
    }

    // ─── RPC queues ──────────────────────────────────────────────────────────
    @Bean
    public Queue rpcLivrosCriarQueue() { return QueueBuilder.durable(RPC_LIVROS_CRIAR).build(); }

    @Bean
    public Queue rpcLivrosListarQueue() { return QueueBuilder.durable(RPC_LIVROS_LISTAR).build(); }

    @Bean
    public Queue rpcLivrosBuscarQueue() { return QueueBuilder.durable(RPC_LIVROS_BUSCAR).build(); }

    @Bean
    public Queue rpcLivrosAtualizarQueue() { return QueueBuilder.durable(RPC_LIVROS_ATUALIZAR).build(); }

    @Bean
    public Queue rpcLivrosExcluirQueue() { return QueueBuilder.durable(RPC_LIVROS_EXCLUIR).build(); }

    @Bean
    public Queue rpcAutoresCriarQueue() { return QueueBuilder.durable(RPC_AUTORES_CRIAR).build(); }

    @Bean
    public Queue rpcAutoresListarQueue() { return QueueBuilder.durable(RPC_AUTORES_LISTAR).build(); }

    @Bean
    public Queue rpcAutoresBuscarQueue() { return QueueBuilder.durable(RPC_AUTORES_BUSCAR).build(); }

    @Bean
    public Queue rpcAutoresAtualizarQueue() { return QueueBuilder.durable(RPC_AUTORES_ATUALIZAR).build(); }

    @Bean
    public Queue rpcAutoresExcluirQueue() { return QueueBuilder.durable(RPC_AUTORES_EXCLUIR).build(); }

    @Bean
    public Queue rpcAssuntosCriarQueue() { return QueueBuilder.durable(RPC_ASSUNTOS_CRIAR).build(); }

    @Bean
    public Queue rpcAssuntosListarQueue() { return QueueBuilder.durable(RPC_ASSUNTOS_LISTAR).build(); }

    @Bean
    public Queue rpcAssuntosBuscarQueue() { return QueueBuilder.durable(RPC_ASSUNTOS_BUSCAR).build(); }

    @Bean
    public Queue rpcAssuntosAtualizarQueue() { return QueueBuilder.durable(RPC_ASSUNTOS_ATUALIZAR).build(); }

    @Bean
    public Queue rpcAssuntosExcluirQueue() { return QueueBuilder.durable(RPC_ASSUNTOS_EXCLUIR).build(); }

    @Bean
    public Queue rpcRelatorioGerarQueue() { return QueueBuilder.durable(RPC_RELATORIO_GERAR).build(); }

    // ─── RPC bindings ────────────────────────────────────────────────────────
    @Bean
    public Binding bindingRpcLivrosCriar() {
        return BindingBuilder.bind(rpcLivrosCriarQueue()).to(livrosRpcExchange()).with(RPC_LIVROS_CRIAR);
    }

    @Bean
    public Binding bindingRpcLivrosListar() {
        return BindingBuilder.bind(rpcLivrosListarQueue()).to(livrosRpcExchange()).with(RPC_LIVROS_LISTAR);
    }

    @Bean
    public Binding bindingRpcLivrosBuscar() {
        return BindingBuilder.bind(rpcLivrosBuscarQueue()).to(livrosRpcExchange()).with(RPC_LIVROS_BUSCAR);
    }

    @Bean
    public Binding bindingRpcLivrosAtualizar() {
        return BindingBuilder.bind(rpcLivrosAtualizarQueue()).to(livrosRpcExchange()).with(RPC_LIVROS_ATUALIZAR);
    }

    @Bean
    public Binding bindingRpcLivrosExcluir() {
        return BindingBuilder.bind(rpcLivrosExcluirQueue()).to(livrosRpcExchange()).with(RPC_LIVROS_EXCLUIR);
    }

    @Bean
    public Binding bindingRpcAutoresCriar() {
        return BindingBuilder.bind(rpcAutoresCriarQueue()).to(livrosRpcExchange()).with(RPC_AUTORES_CRIAR);
    }

    @Bean
    public Binding bindingRpcAutoresListar() {
        return BindingBuilder.bind(rpcAutoresListarQueue()).to(livrosRpcExchange()).with(RPC_AUTORES_LISTAR);
    }

    @Bean
    public Binding bindingRpcAutoresBuscar() {
        return BindingBuilder.bind(rpcAutoresBuscarQueue()).to(livrosRpcExchange()).with(RPC_AUTORES_BUSCAR);
    }

    @Bean
    public Binding bindingRpcAutoresAtualizar() {
        return BindingBuilder.bind(rpcAutoresAtualizarQueue()).to(livrosRpcExchange()).with(RPC_AUTORES_ATUALIZAR);
    }

    @Bean
    public Binding bindingRpcAutoresExcluir() {
        return BindingBuilder.bind(rpcAutoresExcluirQueue()).to(livrosRpcExchange()).with(RPC_AUTORES_EXCLUIR);
    }

    @Bean
    public Binding bindingRpcAssuntosCriar() {
        return BindingBuilder.bind(rpcAssuntosCriarQueue()).to(livrosRpcExchange()).with(RPC_ASSUNTOS_CRIAR);
    }

    @Bean
    public Binding bindingRpcAssuntosListar() {
        return BindingBuilder.bind(rpcAssuntosListarQueue()).to(livrosRpcExchange()).with(RPC_ASSUNTOS_LISTAR);
    }

    @Bean
    public Binding bindingRpcAssuntosBuscar() {
        return BindingBuilder.bind(rpcAssuntosBuscarQueue()).to(livrosRpcExchange()).with(RPC_ASSUNTOS_BUSCAR);
    }

    @Bean
    public Binding bindingRpcAssuntosAtualizar() {
        return BindingBuilder.bind(rpcAssuntosAtualizarQueue()).to(livrosRpcExchange()).with(RPC_ASSUNTOS_ATUALIZAR);
    }

    @Bean
    public Binding bindingRpcAssuntosExcluir() {
        return BindingBuilder.bind(rpcAssuntosExcluirQueue()).to(livrosRpcExchange()).with(RPC_ASSUNTOS_EXCLUIR);
    }

    @Bean
    public Binding bindingRpcRelatorioGerar() {
        return BindingBuilder.bind(rpcRelatorioGerarQueue()).to(livrosRpcExchange()).with(RPC_RELATORIO_GERAR);
    }

    // ─── Message converter ────────────────────────────────────────────────────
    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
