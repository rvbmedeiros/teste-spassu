package com.spassu.livros.microservice.infrastructure.messaging;

import com.spassu.livros.microservice.domain.event.LivroEvent;
import com.spassu.livros.microservice.domain.model.Livro;
import com.spassu.livros.microservice.infrastructure.config.RabbitMqConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.doThrow;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class LivroEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private LivroEventPublisher publisher;

    @Test
    @DisplayName("publishCreated deve enviar evento ao exchange configurado")
    void publishCreated_deveEnviarEventoAoExchangeConfigurado() {
        publisher.publishCreated(Livro.builder().codL(10).titulo("DDD").build());

        then(rabbitTemplate).should().convertAndSend(eq(RabbitMqConfig.EXCHANGE), eq(RabbitMqConfig.RK_CREATED), any(LivroEvent.class));
    }

    @Test
    @DisplayName("publishDeleted nao deve propagar excecao de mensageria")
    void publishDeleted_naoDevePropagarExcecaoDeMensageria() {
        doThrow(new RuntimeException("broker down"))
            .when(rabbitTemplate).convertAndSend(eq(RabbitMqConfig.EXCHANGE), eq(RabbitMqConfig.RK_DELETED), any(LivroEvent.class));

        publisher.publishDeleted(99);

        then(rabbitTemplate).should().convertAndSend(eq(RabbitMqConfig.EXCHANGE), eq(RabbitMqConfig.RK_DELETED), any(LivroEvent.class));
    }
}