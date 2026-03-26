package com.spassu.livros.microservice.infrastructure.messaging;

import com.spassu.livros.microservice.domain.event.LivroEvent;
import com.spassu.livros.microservice.domain.model.Livro;
import com.spassu.livros.microservice.infrastructure.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LivroEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishCreated(Livro livro) {
        publish(LivroEvent.created(livro), RabbitMqConfig.RK_CREATED);
    }

    public void publishUpdated(Livro livro) {
        publish(LivroEvent.updated(livro), RabbitMqConfig.RK_UPDATED);
    }

    public void publishDeleted(Integer id) {
        publish(LivroEvent.deleted(id), RabbitMqConfig.RK_DELETED);
    }

    private void publish(LivroEvent event, String routingKey) {
        try {
            rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE, routingKey, event);
            log.info("Published {} event for livro id={}", event.getType(), event.getLivroCodL());
        } catch (Exception ex) {
            log.warn("Failed to publish {} event: {}", event.getType(), ex.getMessage());
        }
    }
}
