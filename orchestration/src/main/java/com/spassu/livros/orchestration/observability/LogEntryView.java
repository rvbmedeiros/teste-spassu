package com.spassu.livros.orchestration.observability;

import java.time.Instant;

public record LogEntryView(
        long sequence,
        String source,
        Instant timestamp,
        String level,
        String logger,
        String message,
        String thread,
        String requestId,
        String traceId) {
}