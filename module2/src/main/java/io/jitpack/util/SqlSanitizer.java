package io.jitpack.util;

public final class SqlSanitizer {

    private SqlSanitizer() {
        // Utility class
    }

    public static String sanitizeUserInput(String input) {
        if (input == null) {
            return null;
        }
        // Basic sanitization: reject single quotes to reduce SQL injection risk.
        if (input.contains("'")) {
            throw new IllegalArgumentException("Input contains invalid characters.");
        }
        return input;
    }
}
