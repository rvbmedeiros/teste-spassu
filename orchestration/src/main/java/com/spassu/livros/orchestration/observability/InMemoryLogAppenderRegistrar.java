package com.spassu.livros.orchestration.observability;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import jakarta.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class InMemoryLogAppenderRegistrar {

    private static final String APPENDER_NAME = "IN_MEMORY_RUNTIME";

    @PostConstruct
    public void registerAppender() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);

        rootLogger.detachAppender(APPENDER_NAME);

        AppenderBase<ILoggingEvent> runtimeAppender = new AppenderBase<>() {
            @Override
            protected void append(ILoggingEvent eventObject) {
                InMemoryLogStore.getInstance().append(eventObject);
            }
        };

        runtimeAppender.setName(APPENDER_NAME);
        runtimeAppender.setContext(context);
        runtimeAppender.start();
        rootLogger.addAppender(runtimeAppender);
    }
}
