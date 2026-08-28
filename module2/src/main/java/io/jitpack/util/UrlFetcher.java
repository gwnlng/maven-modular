package io.jitpack.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Set;

public class UrlFetcher {
    // Only allow explicit, trusted domains
    private static final Set<String> ALLOWED_DOMAINS = Set.of("://trustedpartner.com", "://mysite.com");

    public static String fetchUrlContent(String targetUrl) {
        StringBuilder result = new StringBuilder();
        try {
            // DANGER: Directly passing user input into the URL object
            URL url = new URL(targetUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            }
        } catch (Exception e) {
            return "Error fetching content";
        }
        return result.toString();
    }

    public static boolean isValidUrl(String inputUrl) {
        try {
            // Use URI to parse safely before converting to URL
            URI uri = new URI(inputUrl);
            String host = uri.getHost();
            String scheme = uri.getScheme();

            // Enforce HTTPS and check against the allowlist
            return "https".equalsIgnoreCase(scheme) && ALLOWED_DOMAINS.contains(host);
        } catch (Exception e) {
            return false;
        }
    }

    public static String requireSafe(String targetUrl) {
        StringBuilder result = new StringBuilder();
        try {
            // DANGER: Directly passing user input into the URL object
            if (!isValidUrl(targetUrl)) {
                throw new IllegalArgumentException("Invalid URL");
            }
            URL url = new URL(targetUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            }
        } catch (Exception e) {
            return "Error fetching content";
        }
        return result.toString();
    }
}