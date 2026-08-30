package io.jitpack.helper;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UrlHelper {

    private static final Logger logger = LoggerFactory.getLogger(UrlHelper.class);

    // Only allow explicit, trusted hosts (exact match, no scheme prefix)
    private static final Set<String> ALLOWED_HOSTS = Set.of("trustedpartner.com", "mysite.com");
    private static final int ALLOWED_PORT = 443;

    public static String validateSafeUrl(String inputUrl) {
        if (inputUrl == null) {
            throw new IllegalArgumentException("Invalid URL");
        }
        try {
            // Use URI to parse safely before converting to URL
            URI uri = new URI(inputUrl);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            int port = uri.getPort();

            // Enforce HTTPS, no embedded credentials, default port only, and check against the allowlist
            if (scheme == null || host == null
                    || !"https".equalsIgnoreCase(scheme)
                    || uri.getUserInfo() != null
                    || (port != -1 && port != ALLOWED_PORT)
                    || !ALLOWED_HOSTS.contains(host.toLowerCase(Locale.ROOT))) {
                logger.warn("Rejected URL failing allowlist/scheme/port checks");
                throw new IllegalArgumentException("Invalid URL");
            }

            // Resolve the host and reject anything pointing at internal/private infrastructure.
            // This guards against DNS rebinding: the string check above only validates the
            // hostname, not what address it actually resolves to at connection time.
            // Note: isSiteLocalAddress() covers RFC1918 but not modern IPv6 ULA (fc00::/7).
            for (InetAddress address : InetAddress.getAllByName(host)) {
                if (address.isLoopbackAddress() || address.isLinkLocalAddress()
                        || address.isSiteLocalAddress() || address.isMulticastAddress()
                        || address.isAnyLocalAddress()) {
                    logger.warn("Rejected URL for host {} resolving to non-public address {}", host, address);
                    throw new IllegalArgumentException("Invalid URL");
                }
            }

            return inputUrl;
        } catch (URISyntaxException | UnknownHostException e) {
            throw new IllegalArgumentException("Invalid URL", e);
        }
    }
}
