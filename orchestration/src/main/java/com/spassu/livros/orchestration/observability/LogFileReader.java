package com.spassu.livros.orchestration.observability;

import ch.qos.logback.classic.Level;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LogFileReader {

    private static final Pattern LOG_PATTERN = Pattern.compile(
            "^(?<timestamp>\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3})\\s+"
                    + "(?<level>TRACE|DEBUG|INFO|WARN|ERROR)\\s+"
                    + "\\[(?<thread>[^\\]]+)]\\s+"
                    + "\\[[^\\]]*]\\s+"
                    + "traceId=(?<traceId>\\S*)\\s+"
                    + "requestId=(?<requestId>\\S*)\\s+"
                    + "(?<logger>\\S+)\\s+-\\s(?<message>.*)$");

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private LogFileReader() {
    }

    public static List<LogEntryView> recent(String source, String minLevel, int limit, String search, String logFileName) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
        int minimumLevel = Level.toLevel(minLevel, Level.INFO).toInt();
        String normalizedSearch = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);

        Path logFile = Path.of(logFileName);
        if (!Files.exists(logFile)) {
            return List.of();
        }

        try {
            List<String> lines = Files.readAllLines(logFile);
            List<LogEntryView> output = new ArrayList<>(safeLimit);
            long sequence = 0;
            for (int i = lines.size() - 1; i >= 0 && output.size() < safeLimit; i--) {
                Matcher matcher = LOG_PATTERN.matcher(lines.get(i));
                if (!matcher.matches()) {
                    continue;
                }

                String level = matcher.group("level");
                if (Level.toLevel(level, Level.INFO).toInt() < minimumLevel) {
                    continue;
                }

                String logger = matcher.group("logger");
                String message = matcher.group("message");
                String thread = matcher.group("thread");
                String requestId = normalizeBlank(matcher.group("requestId"));
                String traceId = normalizeBlank(matcher.group("traceId"));
                if (!matchesSearch(normalizedSearch, logger, message, thread, requestId, traceId)) {
                    continue;
                }

                Instant timestamp = LocalDateTime.parse(matcher.group("timestamp"), TIMESTAMP_FORMATTER)
                        .atZone(ZoneId.systemDefault())
                        .toInstant();

                output.add(new LogEntryView(++sequence, source, timestamp, level, logger, message, thread, requestId, traceId));
            }

            return output;
        } catch (IOException ignored) {
            return List.of();
        }
    }

    private static boolean matchesSearch(String search, String logger, String message, String thread, String requestId, String traceId) {
        if (search.isEmpty()) {
            return true;
        }
        return contains(logger, search)
                || contains(message, search)
                || contains(thread, search)
                || contains(requestId, search)
                || contains(traceId, search);
    }

    private static boolean contains(String value, String search) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(search);
    }

    private static String normalizeBlank(String value) {
        if (value == null || value.isBlank() || "-".equals(value)) {
            return null;
        }
        return value;
    }
}
