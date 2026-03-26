package com.spassu.livros.microservice.infrastructure.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public final class InMemoryLogStore {

    private static final int MAX_ENTRIES = 600;
    private static final InMemoryLogStore INSTANCE = new InMemoryLogStore();

    private final Deque<StoredLogEntry> entries = new ArrayDeque<>();
    private final AtomicLong sequence = new AtomicLong();

    private InMemoryLogStore() {
    }

    public static InMemoryLogStore getInstance() {
        return INSTANCE;
    }

    public synchronized void append(ILoggingEvent event) {
        Map<String, String> mdc = event.getMDCPropertyMap();
        entries.addFirst(new StoredLogEntry(
                sequence.incrementAndGet(),
                Instant.ofEpochMilli(event.getTimeStamp()),
                event.getLevel().levelStr,
                event.getLevel().toInt(),
                event.getLoggerName(),
                event.getFormattedMessage(),
                event.getThreadName(),
                mdc.getOrDefault("requestId", null),
                mdc.getOrDefault("traceId", null)));

        while (entries.size() > MAX_ENTRIES) {
            entries.removeLast();
        }
    }

    public synchronized List<LogEntryView> recent(String source, String minLevel, int limit, String search) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
        int minimumLevel = Level.toLevel(minLevel, Level.INFO).toInt();
        String normalizedSearch = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);

        return entries.stream()
                .filter(entry -> entry.levelValue() >= minimumLevel)
                .filter(entry -> normalizedSearch.isEmpty() || entry.matches(normalizedSearch))
                .limit(safeLimit)
                .map(entry -> entry.toView(source))
                .toList();
    }

    private record StoredLogEntry(
            long sequence,
            Instant timestamp,
            String level,
            int levelValue,
            String logger,
            String message,
            String thread,
            String requestId,
            String traceId) {

        private boolean matches(String search) {
            return contains(logger, search)
                    || contains(message, search)
                    || contains(thread, search)
                    || contains(requestId, search)
                    || contains(traceId, search);
        }

        private LogEntryView toView(String source) {
            return new LogEntryView(sequence, source, timestamp, level, logger, message, thread, requestId, traceId);
        }

        private boolean contains(String value, String search) {
            return value != null && value.toLowerCase(Locale.ROOT).contains(search);
        }
    }
}