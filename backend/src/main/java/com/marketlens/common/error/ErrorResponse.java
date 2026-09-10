package com.marketlens.common.error;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        List<FieldIssue> fieldErrors) {

    public record FieldIssue(String field, String message) {
    }

    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(Instant.now(), status, error, message, List.of());
    }

    public static ErrorResponse of(int status, String error, String message, List<FieldIssue> fieldErrors) {
        return new ErrorResponse(Instant.now(), status, error, message, fieldErrors);
    }

    public Map<String, Object> asMap() {
        return Map.of("timestamp", timestamp, "status", status, "error", error, "message", message);
    }
}
