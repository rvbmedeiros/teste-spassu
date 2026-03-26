package com.spassu.livros.orchestration.observability;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class InMemoryLogAppender extends AppenderBase<ILoggingEvent> {

    @Override
    protected void append(ILoggingEvent eventObject) {
        InMemoryLogStore.getInstance().append(eventObject);
    }
}