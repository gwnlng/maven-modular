package io.jitpack.helper;

import java.net.URI;
import java.util.Set;

public class UrlHelper {
    // Only allow explicit, trusted domains
    private static final Set<String> ALLOWED_DOMAINS = Set.of("://trustedpartner.com", "://mysite.com");

    public static string validateSafeUrl(String inputUrl) {
        try {
            // Use URI to parse safely before converting to URL
            URI uri = new URI(inputUrl);
            String host = uri.getHost();
            String scheme = uri.getScheme();

            // Enforce HTTPS and check against the allowlist
            if (!"https".equalsIgnoreCase(scheme) || !ALLOWED_DOMAINS.contains(host)) {
                throw new IllegalArgumentException("Invalid URL");
            }
            return inputUrl;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid URL", e);
        }
    }
}
