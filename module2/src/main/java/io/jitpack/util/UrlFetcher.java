package io.jitpack.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import io.jitpack.helper.UrlHelper;

public class UrlFetcher {

    private static final int MAX_REDIRECTS = 3;
    private static final int CONNECT_TIMEOUT_MS = 5_000;
    private static final int READ_TIMEOUT_MS = 10_000;
    private static final long MAX_RESPONSE_BYTES = 5L * 1024 * 1024; // 5 MB

    public static String requireSafe(String targetUrl) throws IOException {
        String currentUrl = UrlHelper.validateSafeUrl(targetUrl);
        int redirects = 0;
        while (true) {
            HttpURLConnection connection = openValidatedConnection(currentUrl);
            int status = connection.getResponseCode();
            if (isRedirect(status)) {
                if (++redirects > MAX_REDIRECTS) {
                    throw new IllegalArgumentException("Too many redirects");
                }
                String location = connection.getHeaderField("Location");
                if (location == null || location.isBlank()) {
                    throw new IllegalArgumentException("Invalid redirect target");
                }
                String resolvedTarget = new URL(new URL(currentUrl), location).toString();
                // Full re-validation (allowlist, port, userinfo, DNS/IP) on every hop
                currentUrl = UrlHelper.validateSafeUrl(resolvedTarget);
                continue;
            }
            return readBoundedBody(connection);
        }
    }

    private static HttpURLConnection openValidatedConnection(String validatedUrl) throws IOException {
        URL url = new URL(validatedUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setInstanceFollowRedirects(false); // redirects are handled + re-validated manually
        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);
        connection.setRequestMethod("GET");
        connection.connect();
        return connection;
    }

    private static boolean isRedirect(int status) {
        return status == 301 || status == 302 || status == 303 || status == 307 || status == 308;
    }

    private static String readBoundedBody(HttpURLConnection connection) throws IOException {
        StringBuilder result = new StringBuilder();
        long total = 0;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                total += line.length() + 1;
                if (total > MAX_RESPONSE_BYTES) {
                    throw new IllegalStateException("Response too large");
                }
                result.append(line).append('\n');
            }
        }
        return result.toString();
    }
}
