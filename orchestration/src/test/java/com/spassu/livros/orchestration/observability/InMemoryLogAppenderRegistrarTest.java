package com.spassu.livros.orchestration.observability;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryLogAppenderRegistrarTest {

    @Test
    @DisplayName("registerAppender deve registrar appender runtime no root logger")
    void registerAppender_deveRegistrarAppenderRuntimeNoRootLogger() {
        InMemoryLogAppenderRegistrar registrar = new InMemoryLogAppenderRegistrar();
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);
        root.detachAppender("IN_MEMORY_RUNTIME");

        registrar.registerAppender();

        assertThat(root.getAppender("IN_MEMORY_RUNTIME")).isNotNull();
    }
}